import csv
import json
from collections import defaultdict


def json_to_word_count_csv(file_name):
    count_dict = defaultdict(lambda: 0)
    with open(file_name, 'r') as f:
        json_array = json.load(f)
        for obj in json_array:
            count_dict[obj["Name"]] += 1
    with open("count_words.csv", "w") as f:
        csv_writer = csv.writer(f)
        values = count_dict.items()
        for i in range(len(count_dict)):
            print values[i]
            csv_writer.writerow(values[i])


if __name__ == "__main__":
    json_to_word_count_csv("ner.json")