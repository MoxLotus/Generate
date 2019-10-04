package moxlotus.minecraft.generate;

public class Node<T>{
    private Node<T> parent;
    private Node<T> leftNode;
    private Node<T> rightNode;
    private int weight;
    private int size;
    private int depth;
    private T heldObject;

    public static <T> Node<T> makeRoot(int weight, T heldObject){
        return new Node<>(weight, heldObject);
    }

    private Node(int weight, T heldObject){
        this.heldObject = heldObject;
        this.weight = weight;
        size = weight;
    }

    public void addChild(int weight, T heldObject){
        addChild(new Node<>(weight, heldObject));
    }

    private void addChild(Node<T> node){
        if (rightNode == null){
            claimChild(node);
            return;
        }
        if (leftNode.depth >= rightNode.depth) leftNode.addChild(node);
        else rightNode.addChild(node);
    }
    private void claimChild(Node<T> node){
        node.parent = this;
        boolean deepen = leftNode == null;
        if (deepen) leftNode = node;
        else rightNode = node;
        update(node.weight, deepen);
    }
    private void update(int weight, boolean deepen){
        if (deepen) depth++;
        size = this.weight + leftNode.size + (rightNode == null ? 0 : rightNode.size);
        if (parent != null) parent.update(weight, deepen && this == parent.leftNode);
    }
    public T select(int value){
        if (weight >= value) return heldObject;
        value -= weight;
        if (leftNode.size >= value) return leftNode.select(value);
        value -= leftNode.size;
        return rightNode.select(value);
    }
    public int getSize(){
        return size;
    }
}
