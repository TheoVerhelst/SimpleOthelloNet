package simpleothellonet;

import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Implementation of the minimax and alpha-beta search. The implementation is an
 * algorithm similar to negamax.
 */
public class Minimax {

    /**
     * Plain minimax search, without pruning.
     *
     * @param node The root node.
     * @param depth The maximum depth to explore.
     * @param maxPlayer Indicates whether the current layer is a maximising or
     * minimsing player.
     * @param heuristic The heuristic function.
     * @return The result of the minimax search, containing the best node and
     * its heuristic value.
     */
    public Result minimax(Node node, int depth, boolean maxPlayer,
            ToDoubleFunction<Node> heuristic) {
        List<Node> children = node.getChildren();
        double turnFactor = (maxPlayer ? 1 : -1);

        if (depth == 0 || children.isEmpty()) {
            return new Result(null, heuristic.applyAsDouble(node));
        }

        double bestValue = Double.NEGATIVE_INFINITY * turnFactor;
        Node bestNode = null;

        for (Node child : children) {
            Result childResult = minimax(child, depth - 1, !maxPlayer, heuristic);
            if (childResult.heuristicValue * turnFactor > bestValue * turnFactor) {
                bestNode = child;
                bestValue = childResult.heuristicValue;
            }
        }
        return new Result(bestNode, bestValue);
    }

    /**
     * Alpha-beta search.
     *
     * @param node The root node.
     * @param depth The maximum depth to explore.
     * @param alpha The current alpha value.
     * @param beta The current beta value.
     * @param maxPlayer Indicates whether the current layer is a maximising or
     * minimsing player.
     * @param heuristic The heuristic function.
     * @return The result of the minimax search, containing the best node and
     * its heuristic value.
     */
    public Result alphaBeta(Node node, int depth, double alpha, double beta,
            boolean maxPlayer, ToDoubleFunction<Node> heuristic) {
        List<Node> children = node.getChildren();
        double turnFactor = (maxPlayer ? 1 : -1);

        if (depth == 0 || children.isEmpty()) {
            return new Result(null, heuristic.applyAsDouble(node));
        }

        double bestValue = Double.NEGATIVE_INFINITY * turnFactor;
        Node bestNode = null;

        for (Node child : children) {
            Result childResult = alphaBeta(child, depth - 1, alpha, beta, !maxPlayer, heuristic);
            if (childResult.heuristicValue * turnFactor > bestValue * turnFactor) {
                bestNode = child;
                bestValue = childResult.heuristicValue;
            }
            if (maxPlayer) {
                alpha = Math.max(alpha, bestValue);
            } else {
                beta = Math.min(beta, bestValue);
            }
            if (beta <= alpha) {
                break;
            }
        }
        return new Result(bestNode, bestValue);
    }

    /**
     * Class containing the result of a minimax search.
     */
    public static class Result {

        /**
         * The most promising node.
         */
        public Node node;

        /**
         * The heuristic value on the node.
         */
        public double heuristicValue;

        public Result(Node node, double heuristicValue) {
            this.node = node;
            this.heuristicValue = heuristicValue;
        }
    }
}
