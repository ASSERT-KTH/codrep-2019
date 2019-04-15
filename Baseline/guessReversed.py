from pathlib import Path

def main():
    path_to_datasets = Path(__file__).absolute().parent.parent / "Datasets"
    for path_to_dataset in path_to_datasets.iterdir():
        if(path_to_dataset.is_dir()):
            for task in path_to_dataset.iterdir():
                if(task.name.endswith(".txt") and task.name != "out.txt"):
                    with open(task, "r") as file:
                        reversedGuess = list(range(len(file.read()), 0, -1))
                        print(str(task) + " " + ' '.join(str(pos) for pos in reversedGuess), flush=True)

if __name__ == "__main__":
    main()
