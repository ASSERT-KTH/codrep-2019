from pathlib import Path
from random import shuffle

def main():
    path_to_datasets = Path(__file__).absolute().parent.parent / "Datasets"
    for path_to_dataset in path_to_datasets.iterdir():
        if(path_to_dataset.is_dir()):
            for task in path_to_dataset.iterdir():
                if(task.name.endswith(".txt") and task.name != "out.txt"):
                    with open(task, "r") as file:
                        randomGuess = list(range(1, len(file.read())+1))
                        shuffle(randomGuess)
                        print(str(task) + " " + ' '.join(str(pos) for pos in randomGuess), flush=True)

if __name__ == "__main__":
    main()
