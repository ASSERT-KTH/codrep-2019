import sys
import getopt
import errno
from pathlib import Path

# Total number of files in chosen datasets
total_files = 0
# Default score of all prediction
score = {}
# All prediction outputs by your algorithm
all_predictions = {}

# When predicting files outside of chosen datasets
class DatasetsNotChosenException(Exception):
    pass

# When predicting the same file twice
class MultiplePredictionsFoundException(Exception):
    pass

def AveragePrecision(prediction, solution):
    # Loss function, more information in README
    for index, pred in enumerate(prediction):
        if(pred == solution):
            return 1/(index+1)
    return 0

# Count files
def countTasks(chosen_datasets):
    count = 0

    if(chosen_datasets):
        for path_to_dataset in chosen_datasets:
            for task in path_to_dataset.iterdir():
                if(task.name.endswith(".txt") and task.name != "out.txt"):
                    count += 1
    else:
        path_to_datasets = Path(__file__).absolute().parent.parent / "Datasets"
        for path_to_dataset in path_to_datasets.iterdir():
            if(path_to_dataset.is_dir()):
                for task in path_to_dataset.iterdir():
                    if(task.name.endswith(".txt") and task.name != "out.txt"):
                        count += 1
    return count

# Init score for files, default score is 0 for each file
def initScore(chosen_datasets):
    score = {}

    if(chosen_datasets):
        for path_to_dataset in chosen_datasets:
            for task in path_to_dataset.iterdir():
                if(task.name.endswith(".txt") and task.name != "out.txt"):
                    score[task.absolute()] = 0
    else:
        path_to_datasets = Path(__file__).absolute().parent.parent / "Datasets"
        for path_to_dataset in path_to_datasets.iterdir():
            if(path_to_dataset.is_dir()):
                for task in path_to_dataset.iterdir():
                    if(task.name.endswith(".txt") and task.name != "out.txt"):
                        score[task.absolute()] = 0
    return score

# Check the answers against the solution
def checkAnswers(prediction, path_to_task, chosen_datasets):
    global score

    # Get the solution
    solutions_line = int(path_to_task.name.split(".")[0])
    with open(path_to_task.parent / "out.txt", "r") as file:
        for line, solution in enumerate(file):
            if(line == solutions_line):
                solution = int(solution)
                break

    # Check if the task if inside of chosen datasets
    if(chosen_datasets):
        isInChosenDatasets = False
        for path_to_dataset in chosen_datasets:
            if(str(path_to_dataset.absolute()) in str(path_to_task.absolute())):
                isInChosenDatasets = True
        if(not isInChosenDatasets):
            raise DatasetsNotChosenException(str(path_to_task) + " is outside of chosen datasets.")

    # Check if the file is already predicted
    if(path_to_task in all_predictions):
        raise MultiplePredictionsFoundException("Multiple predictions to " + str(path_to_task))

    # Stored for later use, maybe in verbose mode?
    all_predictions[path_to_task] = prediction

    # Calculate the loss and update the score
    averagePrecision = AveragePrecision(prediction, solution)
    score[path_to_task] = averagePrecision

# Print neccesary statistics
def printStatistics(verbose):
    global total_files, score
    print("Total files: " + str(total_files))
    print("MAP: " + str(sum(score.values())/(total_files*1.0)) + " (the higher, the better)")

def main():
    global total_files, score

    # Parse the options
    verbose = False #TODO, verbose mode?
    chosen_datasets = None
    try:
        opts, args = getopt.getopt(sys.argv[1:], "d:vh", ["datasets=","help"])
    except getopt.GetoptError:
        raise
    for opt, arg in opts:
        if opt == "-v":
            verbose = True
        elif opt in ("-d", "--datasets"):
            chosen_datasets = arg.split(":")
            chosen_datasets = [Path(path_to_dataset) for path_to_dataset in chosen_datasets]
        elif opt in ("-h", "--help"):
            print("usage evaluate.py [-vh] [-d path] [--datasets=path] [--help]")
            print("-v for verbose output mode")
            print("-d or --datasets= to evaluate on chosen datasets, must be absolute path, multiple paths should be seperated with ':'. Default is evaluating on all datasets")
            sys.exit()

    # Count total number of tasks, default is all tasks in Datasets/
    total_files = countTasks(chosen_datasets)
    # Deafault score of 0 for each tasks, which is maximal loss
    score = initScore(chosen_datasets)

    # Reading each predition
    for args in sys.stdin:
        inputs = args.split()
        path_to_task = Path(inputs[0])
        prediction = inputs[1:]

        if(not path_to_task.exists()):
            raise FileNotFoundError(errno.ENOENT, os.strerror(errno.ENOENT), path_to_task)

        try:
            prediction = [int(pos) for pos in prediction]
        except ValueError:
            print(prediction + " should only contain integers!")
            raise

        # Check the prediction
        checkAnswers(prediction, path_to_task, chosen_datasets)

    # Print statistics about your algorithm
    printStatistics(verbose)

if __name__=="__main__":
    main()
