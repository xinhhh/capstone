import datetime
import logging
import numpy as np
import pytorch_lightning as pl
import torch
import torch.optim
import torch.nn
from py4ml.utils.util import calculate_metrics

def get_standard_params_for_trainer(metric, save_checkpoints=True):

    # https://pytorch-lightning.readthedocs.io/en/latest/generated/pytorch_lightning.callbacks.ModelCheckpoint.html
    # By default, dirpath is None and will be set at runtime to the location specified
    # by Trainer’s default_root_dir or weights_save_path arguments,
    # and if the Trainer uses a logger, the path will also contain logger name and version.
    if save_checkpoints:
        # by default, save the checkpoint for the last epoch and the epoch with the best validation error
        checkpoint_callback = pl.callbacks.ModelCheckpoint(monitor=metric, save_last=True, period=1, save_top_k=1)
    else:
        checkpoint_callback = None

    # does not work at the moment on laptop with gpu
    gpus = 1 if torch.cuda.is_available() else 0
    #gpus = 0

    params = {
        'log_every_n_steps': 1,
        'flush_logs_every_n_steps': 10,
        'progress_bar_refresh_rate': 1,
        'checkpoint_callback': checkpoint_callback,
        'gpus': gpus,
    }

    return params

class LightningModelWrapper(pl.LightningModule):

    def __init__(self, optimizer, transformer):
        super().__init__()

        params = {'optimizer': optimizer, 'transformer': transformer}
        logging.info('initializing LightningModelWrapper with %s', params)

        self.optimizer = optimizer
        self.transformer = transformer

        self.test_predictions = None

    def configure_optimizers(self):
        #optimizer = getattr(torch.optim, self.optimizer)(self.parameters(), lr=self.optimizer_lr)
        opt_params = self.optimizer.copy()
        name = opt_params.pop('name')
        optimizer_instance = getattr(torch.optim, name)(self.parameters(), **opt_params)
        logging.info('created optimizer=%s', optimizer_instance)
        return optimizer_instance

    def training_step(self, batch, batch_idx):
        x, y = batch
        y_hat = self(x)
        loss = torch.nn.MSELoss()(y_hat, y)
        self.log('phase', 'train')
        self.log('loss', loss)
        self.log('count', len(y))
        self.log('time', str(datetime.datetime.now()))
        return loss

    def validation_step(self, batch, batch_idx):
        x, y = batch
        y_hat = self(x)
        return y, y_hat

    def validation_epoch_end(self, outputs):
        result, _, _ = shared_epoch_end(
            outputs, is_validation=True, epoch=self.current_epoch,
            transformer=self.transformer)

        for key, value in result.items():
            self.log(key, value)

        return super().validation_epoch_end(outputs)

    def test_step(self, batch, batch_idx):
        x, y = batch
        y_hat = self(x)
        return y, y_hat

    def test_epoch_end(self, outputs):
        result, y, y_hat = shared_epoch_end(
            outputs, is_validation=False, epoch=self.current_epoch,
            transformer=self.transformer)

        for key, value in result.items():
            self.log(key, value)

        self.test_predictions = (y, y_hat)
        return super().test_epoch_end(outputs)

def shared_epoch_end(tensor_step_outputs, is_validation, epoch, transformer):
    y_complete = np.array([])
    y_hat_complete = np.array([])
    for outputs in tensor_step_outputs:
        #y = outputs[0].detach().cpu().numpy()
        #y_complete = np.concatenate((y_complete, y))
        #y_hat = outputs[1].detach().cpu().numpy()
        #y_hat_complete = np.concatenate((y_hat_complete, y_hat))

        y = torch.flatten(outputs[0].detach().cpu())
        y = y.numpy()
        y_complete = np.concatenate((y_complete, y))
        y_hat = torch.flatten(outputs[1].detach().cpu())
        y_hat = y_hat.numpy()
        y_hat_complete = np.concatenate((y_hat_complete, y_hat))

    metrics = calculate_metrics(y_complete, y_hat_complete)

    if is_validation:
        result = {'phase': 'val'}
    else: # test
        result = {'phase': 'test'}

    result.update({
        'epoch': epoch,
        'time': str(datetime.datetime.now()),
    })
    result.update(metrics)
    logging.info('result=%s', result)

    return (result, y_complete, y_hat_complete)