import json
import os
import pickle
import time

import pandas as pd


class HopExtractor:
    """
    The Hop Extractor class extracts n-hop neighbours of a certain entity.
    """

    def __init__(self, dataset_dir, dataset_name, n: int = 3):
        """
        Initialize the extractor instance
        :param dataset_dir: the folder of the triples and indexing files
        :param dataset_name: the names of the dataset
        :param n: the maximum distance between the head entity and the neighbours
        """
        self.dataset_dir = dataset_dir
        self.dataset_name = dataset_name
        self.n = n
        self.triples_full_path = os.path.join(self.dataset_dir, f'{self.dataset_name}-train.txt')
        self.triples = pd.read_csv(self.triples_full_path, sep='\t', header=None)
        self.entity2idx_path = os.path.join(self.dataset_dir, f'entity2idx.pkl')
        self.entity2idx = pickle.load(open(self.entity2idx_path, "rb"))
        self.ent_labels = list(self.entity2idx.keys())
        self.three_hop_dict_label_path = os.path.join(self.dataset_dir, 'three_hop_dict_label')
        self.three_hop_dict_index_path = os.path.join(self.dataset_dir, 'three_hop_dict_index')
        self.three_hop_dict_label = {}
        self.three_hop_dict_index = {}
        self.parse_knowledge_graph()

    def parse_knowledge_graph(self):
        """
        :return: a dictionary mapping node name to a list of neighbours, a dictionary mapping index to a list of
        neighbour's indices
        """
        if os.path.exists(self.three_hop_dict_label_path) and os.path.exists(self.three_hop_dict_index_path):
            self.three_hop_dict_label = json.loads(open(self.three_hop_dict_label_path).read())
            print(f"Loading 3 hop dictionary from {self.three_hop_dict_label_path}")
            self.three_hop_dict_index = json.loads(open(self.three_hop_dict_index_path).read())
            print(f"Loading 3 hop dictionary from {self.three_hop_dict_index_path}")

        else:
            print(f"Creating 3 hop dictionary.")
            one_hop_dict = {}  # label to label dict
            one_hop_idx_dict = {}  # idx to idx dict
            three_hop_dict = {}  # label to label dict
            three_hop_idx_dict = {}  # idx to idx dict
            for entity in self.ent_labels:
                entity_idx = self.entity2idx[entity]
                neighbour_rows = self.triples[self.triples.isin([entity]).any(axis=1)]
                # extract first neighbours
                neighbours = list(
                    set(neighbour_rows.iloc[:, 0].values.tolist() + neighbour_rows.iloc[:, 2].values.tolist()))
                neighbours.remove(entity)
                neighbours_idx = [self.entity2idx[n] for n in neighbours]
                one_hop_dict[entity] = neighbours
                one_hop_idx_dict[entity_idx] = neighbours_idx

            for entity in self.ent_labels:
                entity_idx = self.entity2idx[entity]
                first_neighbours = one_hop_dict[entity]
                second_neighbours = []
                third_neighbours = []
                for first_neighbour in first_neighbours:
                    second_neighbours += one_hop_dict[first_neighbour]
                    for second_neighbour in second_neighbours:
                        third_neighbours += one_hop_dict[second_neighbour]

                three_hop_dict[entity] = list(set(first_neighbours + second_neighbours + third_neighbours))
                three_hop_dict[entity].remove(entity)
                three_hop_idx_dict[entity_idx] = [self.entity2idx[e_idx] for e_idx in three_hop_dict[entity]]

            self.three_hop_dict_label = three_hop_dict
            self.three_hop_dict_index = three_hop_idx_dict

            with open(self.three_hop_dict_label_path, 'w') as f:
                f.write(json.dumps(self.three_hop_dict_label))
                f.close()
            print(f'Writing label dictionary to {self.three_hop_dict_index_path}')

            with open(self.three_hop_dict_index_path, 'w') as f:
                f.write(json.dumps(self.three_hop_dict_index))
                f.close()
            print(f'Writing index dictionary to {self.three_hop_dict_index_path}')

    def extract_neighbour_from_idx(self, entity_idx):
        return self.three_hop_dict_index[str(entity_idx)]


if __name__ == "__main__":
    START_TIME = time.time()
    from Marie.Util.location import DATA_DIR

    my_extractor = HopExtractor(dataset_dir=os.path.join(DATA_DIR, 'ontocompchem_calculation'),
                                dataset_name='ontocompchem_calculation')

    print(time.time() - START_TIME)
