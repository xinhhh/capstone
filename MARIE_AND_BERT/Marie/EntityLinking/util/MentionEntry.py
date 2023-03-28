import json
def load_mention_entries(path):

    assert path is not None, "Error! mention test file is empty."

    mention_list = []
    with open(path, 'rt') as f:
        for line in f:
            sample = json.loads(line.rstrip())
            title = sample['mention']
            cl = sample.get("context_left", "")
            cr = sample.get("context_right", "")
            mention_list.append((title, cl, cr))

    return mention_list