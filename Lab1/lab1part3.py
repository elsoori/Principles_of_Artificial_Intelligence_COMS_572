# import required module
import glob
import re
import math
import signal
import time
import sys


# Needed to hide warnings in the matplotlib sections
import warnings
from search import *

warnings.filterwarnings("ignore")
# %matplotlib inline

# Heuristics for 8 Puzzle Problem

class TimeoutError(Exception):
    """
    Custom error for Timeout class.
    """

    pass


class Timeout:
    """
    A timeout handler with context manager.
    Based on UNIX signals.
    """

    def __init__(self, seconds=1, error_message="Timeout"):
        self.seconds = seconds
        self.error_message = error_message

    def handle_timeout(self, signum, frame):
        raise TimeoutError(self.error_message)

    def __enter__(self):
        signal.signal(signal.SIGALRM, self.handle_timeout)
        signal.alarm(self.seconds)

    def __exit__(self, type, value, traceback):
        signal.alarm(0)


def linear(node):
    return sum([1 if node.state[i] != goal[i] else 0 for i in range(8)])


def manhattan(node):
    state = node.state
    index_goal = {0: [2, 2], 1: [0, 0], 2: [0, 1], 3: [0, 2], 4: [1, 0], 5: [1, 1], 6: [1, 2], 7: [2, 0], 8: [2, 1]}
    index_state = {}
    index = [[0, 0], [0, 1], [0, 2], [1, 0], [1, 1], [1, 2], [2, 0], [2, 1], [2, 2]]
    x, y = 0, 0

    for i in range(len(state)):
        index_state[state[i]] = index[i]

    mhd = 0

    for i in range(8):
        for j in range(2):
            mhd = abs(index_goal[i][j] - index_state[i][j]) + mhd

    return mhd


def sqrt_manhattan(node):
    state = node.state
    index_goal = {0: [2, 2], 1: [0, 0], 2: [0, 1], 3: [0, 2], 4: [1, 0], 5: [1, 1], 6: [1, 2], 7: [2, 0], 8: [2, 1]}
    index_state = {}
    index = [[0, 0], [0, 1], [0, 2], [1, 0], [1, 1], [1, 2], [2, 0], [2, 1], [2, 2]]
    x, y = 0, 0

    for i in range(len(state)):
        index_state[state[i]] = index[i]

    mhd = 0

    for i in range(8):
        for j in range(2):
            mhd = (index_goal[i][j] - index_state[i][j]) ** 2 + mhd

    return math.sqrt(mhd)


def max_heuristic(node):
    score1 = manhattan(node)
    score2 = linear(node)
    return max(score1, score2)

if __name__=="__main__":

    # if len(sys.argv < 3):
    #     print("Not enough argument")
    #     exit()
    pathname =sys.argv[1]
    # algorithm = sys.argv[2]
    # assign directory
    directory = pathname
    goal = [1, 2, 3, 4, 5, 6, 7, 8, 0]
    maxtime = 900
    BFSavg = 0
    BFSand = 0

    IDSavg = 0
    IDSand = 0

    Ah1avg = 0
    Ah1and = 0

    Ah2avg = 0
    Ah2and = 0

    Ah3avg = 0
    Ah3and = 0
    # iterate over files in
    # that directory
    filecount= 0
    for filename in glob.iglob(f'{directory}/*'):
        filecount = filecount + 1
        if filename.endswith('.txt'):
            a_file = open(filename, "r")

        contents = a_file.read()
        output = re.sub(r"[\n\t\s]*", "", contents)
        lst = []

        for letter in contents:
            lst.append(letter)

        arr = list(output)
        for i in range(0 , len(arr)):
            if arr[i] == '_':
                arr[i] = 0
            elif arr[i] == '1':
                arr[i] = 1
            elif arr[i] == '2':
                arr[i] = 2
            elif arr[i] == '3':
                arr[i] = 3
            elif arr[i] == '4':
                arr[i] = 4
            elif arr[i] == '5':
                arr[i] = 5
            elif arr[i] == '6':
                arr[i] = 6
            elif arr[i] == '7':
                arr[i] = 7
            elif arr[i] == '8':
                arr[i] = 8
        a_file.close()

        print("\n", filename)
        puzzle = EightPuzzle(tuple(arr))
        try:
            assert puzzle.check_solvability(arr)
        except:
            print("puzzle is unsolvable")
            break

        filecount = filecount + 1

        with Timeout(maxtime):
            try:
                start = time.time()
                sol = breadth_first_graph_search(puzzle).solution()
                end = time.time()
                time0 = (end - start)

                BFSavg = BFSavg + time0
                print(" Time taken for BFS =", time0, "s")
                print("Path length:", len(sol))
                print(sol)
            except TimeoutError:
                sol = None
        print("\n")

        with Timeout(maxtime):
            try:
                start1 = time.time()
                sol1 = iterative_deepening_search(puzzle).solution()
                end1 = time.time()
                time1 = (end1 - start1)

                IDSavg = IDSavg + time1
                print(" Time taken for IDS =", time1, "s")
                print("Path length:", len(sol1))
                print(sol1)
            except TimeoutError:
                sol1 = None
        print("\n")

        # A* search
        with Timeout(maxtime):
            try:
                start2 = time.time()
                sol2 = astar_search(puzzle, h=linear).solution()
                end2 = time.time()
                time2 = (end2 - start2)

                Ah1avg = Ah1avg + time2
                print(" Time taken for A*linear =", time2, "s")
                print("Path length:", len(sol2))
                print(sol2)
            except TimeoutError:
                sol2 = None

        print("\n")

        with Timeout(maxtime):
            try:
                start3 = time.time()
                sol3 = astar_search(puzzle, h=manhattan).solution()
                end3 = time.time()
                time3 = (end3 - start3)

                Ah2avg = Ah2avg + time3
                print(" Time taken for A* manhattan =", time3, "s")
                print("Path length:", len(sol3))
                print(sol3)
            except TimeoutError:
                sol3 = None

        print("\n")
        with Timeout(maxtime):
            try:
                start4 = time.time()
                sol4 = astar_search(puzzle, h=max_heuristic).solution()
                end4 = time.time()
                time4 = (end4 - start4)

                Ah3avg = Ah3avg + time4
                print(" Time taken for A* max_heuristics =", time4, "s")
                print("Path length:", len(sol4))
                print(sol4)
            except TimeoutError:
                sol4 = None

    print("\n")
    BFSavg = BFSavg/filecount

    print("BFS Average time:", BFSavg)

    print("\n")
    IDSavg = IDSavg / filecount

    print("IDS Average time:", IDSavg)

    print("\n")
    Ah1avg = Ah1avg / filecount

    print("A* H1 Average time:", Ah1avg)

    print("\n")
    Ah2avg = Ah2avg / filecount

    print("A* H2 Average time:", Ah2avg)

    print("\n")
    Ah3avg = Ah3avg / filecount

    print("A* H3 Average time:", Ah3avg)

