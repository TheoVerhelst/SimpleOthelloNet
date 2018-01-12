# SimpleOthelloNet

## What's this?
This is a simple attempt to use a neural network to learn how to play the [Othello game](https://en.wikipedia.org/wiki/Reversi).
It turns out that the neural network beats a minimax player at depth of 6 with a good heuristic designed by T. Yoshioka
(see the doc for further references).

## How does it work?
The network is trained by modifying its weights using temporal difference learning. It plays against a copy of itself, and takes
the raw board as input. After a few 45,000 games, it becomes a decent player. See the doc for further explanations.

## How can I try it?
It's pure java, with only the help of Neuroph library. Once you set up the project in your IDE and imported Neuroph,
just run it. By default, the main function trains a neural network using some parameters,
and save them to a file. You can edit it to do whatever you want.
