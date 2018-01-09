package simpleothellonet;

import java.util.List;

/**
 * Interface used to model a hierarchy of any kind.
 */
public interface Node {

    /**
     * Returns the parent node from which this one was expanded, or null if it
     * is the root node.
     *
     * @return The parent as desribed above.
     */
    public Node getParent();

    /**
     * Generates all children of this node.
     *
     * @return All children.
     */
    public List<Node> getChildren();
}
