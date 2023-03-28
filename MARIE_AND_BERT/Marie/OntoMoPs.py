import os, sys

sys.path.append("..")
from Marie.QAEngineNumerical import QAEngineNumerical
from Marie.Util.location import DATA_DIR


class OntoMoPsQAEngine(QAEngineNumerical):

    def __init__(self, dataset_dir, dataset_name, sub_ontology):
        self.dataset_dir = dataset_dir
        self.ontology = ontology
        self.sub_ontology = sub_ontology
        super().__init__(dataset_dir, dataset_name, dim=50, test=False, value_dict_name="node_value_dict.json",
                         seperated_model=True, operator_dict={0: "smaller", 1: "larger", 2: "none"})


if __name__ == "__main__":
    ontology = "OntoMoPs"
    sub_ontology = "numerical_with_implicit"
    dataset_dir = os.path.join(DATA_DIR, f"CrossGraph/{ontology}/{sub_ontology}")
    my_engine = OntoMoPsQAEngine(dataset_dir=dataset_dir, dataset_name=sub_ontology, sub_ontology=sub_ontology)
    rst = my_engine.run("MoPs with molecular weight more than 10")
    print(rst)