import json
from collections import defaultdict

import operator


def max_joint_agreement(nltk_file, open_file, core_file, output_file):
    count_dict = defaultdict(lambda: 0)
    nltk_dict = defaultdict(lambda: 0)
    core_dict = defaultdict(lambda: 0)
    open_dict = defaultdict(lambda: 0)
    with open(nltk_file, "r") as f:
        for line in f.readlines():
            words = line.split(",")
            for word in words:
                word = word.strip().lower()
                count_dict[word] += 1
                nltk_dict[word] += 1
    with open(core_file, "r") as f:
        for line in f.readlines():
            words = line.split(",")
            for word in words:
                word = word.strip().lower()
                count_dict[word] += 1
                core_dict[word] += 1
    with open(open_file, "r") as f:
        for line in f.readlines():
            words = line.split(",")
            for word in words:
                word = word.strip().lower()
                count_dict[word] += 1
                open_dict[word] += 1
    count_dict = sorted(count_dict.items(), key=operator.itemgetter(1), reverse=True)
    nltk_list = []
    core_list = []
    open_list = []
    labels = []
    for item in count_dict[:100]:
        if item[0]:
            labels.append(item[0])
            nltk_list.append(nltk_dict[item[0]])
            core_list.append(core_dict[item[0]])
            open_list.append(open_dict[item[0]])
    json_dict = {"labels": labels,
                 "series": [{"label": "nltk", "values": nltk_list}, {"label": "opennlp", "values": open_list},
                            {"label": "corenlp", "values": core_list}]}
    with open(output_file, "w") as f:
        json.dump(json_dict, f)

if __name__ == "__main__":
    max_joint_agreement("nltk.txt", "open.txt", "core.txt", "max_joint.json")
