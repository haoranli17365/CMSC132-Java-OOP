package avlg;

import avlg.exceptions.UnimplementedMethodException;
import avlg.exceptions.EmptyTreeException;
import avlg.exceptions.InvalidBalanceException;

/** <p>{@link AVLGTree}  is a class representing an <a href="https://en.wikipedia.org/wiki/AVL_tree">AVL Tree</a> with
 * a relaxed balance condition. Its constructor receives a strictly  positive parameter which controls the <b>maximum</b>
 * imbalance allowed on any subtree of the tree which it creates. So, for example:</p>
 *  <ul>
 *      <li>An AVL-1 tree is a classic AVL tree, which only allows for perfectly balanced binary
 *      subtrees (imbalance of 0 everywhere), or subtrees with a maximum imbalance of 1 (somewhere). </li>
 *      <li>An AVL-2 tree relaxes the criteria of AVL-1 trees, by also allowing for subtrees
 *      that have an imbalance of 2.</li>
 *      <li>AVL-3 trees allow an imbalance of 3.</li>
 *      <li>...</li>
 *  </ul>
 *
 *  <p>The idea behind AVL-G trees is that rotations cost time, so maybe we would be willing to
 *  accept bad search performance now and then if it would mean less rotations. On the other hand, increasing
 *  the balance parameter also means that we will be making <b>insertions</b> faster.</p>
 *
 * @author Haoran Li
 *
 * @see EmptyTreeException
 * @see InvalidBalanceException
 * @see StudentTests
 */
public class AVLGTree<T extends Comparable<T>> {

    /* ********************************************************* *
     * Write any private data elements or private methods here...*
     * ********************************************************* */
    private class Node{
        private T data;
        private Node left;
        private Node right;
        // node constructor
        private Node(T otherData){
            this.data = otherData;
            this.left = null;
            this.right = null;
        }
        private Node(Node other){
            this.data = other.data;
            this.left = other.left;
            this.right = other.right;
        }
    }

    private int size; // total number of node in the tree
    private int maxImbalance;
    private Node root; // define root of the AVL tree.

    /**
     * rotate left method based on the current node as root.
     * @param target
     */
    private Node rotateLeft(Node target){
        Node newRoot = target.right;
        target.right = newRoot.left;
        newRoot.left = target;
        return newRoot;
    }

    /**
     * rotate right method based on the current node as root
     * @param target
     */
    private Node rotateRight(Node target){
        Node newRoot = target.left;
        target.left = newRoot.right;
        newRoot.right = target;
        return newRoot;
    }

    /**
     * rotate left-right method based on the current node as root
     * @param target
     */
    private Node rotateLeftRight(Node target){
        target.left = rotateLeft(target.left);
        target = rotateRight(target);
        return target;
    }

    /**
     * rotate right-left method based on the current node as root
     * @param target
     */
    private Node rotateRightLeft(Node target){
        target.right = rotateRight(target.right);
        target = rotateLeft(target);
        return target;
    }


    /**
     * find the height of the current node
     * @param curr
     * @return
     */
    private int getNodeHeight(Node curr){
        if (curr == null){
            return -1;
        }else if (curr.left == null && curr.right == null){
            return 0;
        }else{
            return Math.max(getNodeHeight(curr.left),getNodeHeight(curr.right)) + 1;
        }
    }

    /* ******************************************************** *
     * ************************ PUBLIC METHODS **************** *
     * ******************************************************** */

    /**
     * The class constructor provides the tree with the maximum imbalance allowed.
     * @param maxImbalance The maximum imbalance allowed by the AVL-G Tree.
     * @throws InvalidBalanceException if maxImbalance is a value smaller than 1.
     */
    public AVLGTree(int maxImbalance) throws InvalidBalanceException {
        if (maxImbalance < 1){
            throw new InvalidBalanceException("Invalid Balance provided");
        }
        this.maxImbalance = maxImbalance;
        this.size = 0;
        this.root = null;
    }

    /**
     * Insert key in the tree. You will <b>not</b> be tested on
     * duplicates! This means that in a deletion test, any key that has been
     * inserted and subsequently deleted should <b>not</b> be found in the tree!
     * s
     * @param key The key to insert in the tree.
     */
    public void insert(T key) {
        Node newNode = new Node(key);
        this.size++;
        if (isEmpty()){
            this.root = newNode;
        }else{
            this.root = insertNode(this.root, newNode);
        }
    }

    /**
     * Insert method helper for just inserting the newNode into the tree without balancing the AVL tree.
     * @param curr
     * @param newNode
     */
    private Node insertNode(Node curr, Node newNode){
        if (curr == null){
            return newNode;
        }else{
            if (newNode.data.compareTo(curr.data) < 0){
                curr.left = insertNode(curr.left, newNode);
                if(Math.abs(getNodeHeight(curr.left) - getNodeHeight(curr.right)) > this.maxImbalance){
                    // Right or Left-Right
                    if (newNode.data.compareTo(curr.left.data) < 0){
                        return this.rotateRight(curr);
                    }else{
                       return this.rotateLeftRight(curr);
                    }
                }else{ // case: current subtree still AVL balanced
                    return curr;
                }
            }else{
                curr.right = insertNode(curr.right, newNode);
                if(Math.abs(getNodeHeight(curr.left) - getNodeHeight(curr.right)) > this.maxImbalance){
                    // Left or Right-Left
                    if (newNode.data.compareTo(curr.right.data) > 0){
                        return this.rotateLeft(curr);
                    }else{
                        return this.rotateRightLeft(curr);
                    }
                }else{ // case: current subtree still AVL balanced
                    return curr;
                }
            }
        }
    }

    /**
     * Delete the key from the data structure and return it to the caller.
     * @param key The key to delete from the structure.
     * @return The key that was removed, or {@code null} if the key was not found.
     * @throws EmptyTreeException if the tree is empty.
     */
    public T delete(T key) throws EmptyTreeException {
        if(isEmpty()){
            throw new EmptyTreeException("Empty Tree for deletion");
        }else{
            Node target_node = searchNode(this.root, key);
            if (target_node == null){ // can find the target.
                return null;
            }else{
                T ret = target_node.data;
                this.size --;
                // target is root
                if(this.root.data.compareTo(key) == 0){ // deleting the root.
                    Node inOrderSucc = findInOrderSuccessor(this.root);
                    if (inOrderSucc == null){
                        // don't have in-order successor
                        this.root = this.root.left;
                        return ret;
                    }else{
                        // have in order successor
                        T original_root_data = this.root.data;
                        this.root = deleteHelper(this.root, inOrderSucc.data);
                        if (this.root.data.compareTo(original_root_data) == 0){ // root stay as root after deletion.
                            this.root.data = inOrderSucc.data;
                        }else if(this.root.left != null && this.root.left.data.compareTo(original_root_data) == 0){ // the original root becomes the left child
                            this.root.left.data = inOrderSucc.data;
                        }else{ // orginal root becomes the right child
                            this.root.right.data = inOrderSucc.data;
                        }
                        return ret;
                    }
                }else if(target_node.left != null || target_node.right != null){
                    // if target is not root, but is non-leaf
                    Node inOrderSucc = findInOrderSuccessor(target_node); // in-order successor reference
                    if (inOrderSucc == null){
                        // don't have in-order successor
                        Node parent_node = findParent(this.root, key);
                        if (parent_node.left != null && parent_node.left.data.compareTo(target_node.data) == 0){
                            // if the target is the left child of the parent.
                            parent_node.left = target_node.left;
                        }else if (parent_node.right != null && parent_node.right.data.compareTo(target_node.data) == 0){
                            // if the target is the right child of the parent.
                            parent_node.right = target_node.left;
                        }
                        return ret;
                    }else{
                        // have in-order successor
                        T target_data = target_node.data;
                        this.root = deleteHelper(this.root, inOrderSucc.data);
                        Node after_target_node = searchNode(this.root, target_data);
                        after_target_node.data = inOrderSucc.data;
                        return ret;
                    }
                }else{
                    // target is leaf
                    this.root = deleteHelper(this.root, key);
                    return ret;
                }
            }
        }
    }
    /**
     * This deletion helper only care about deleting the leaf node.
     * @param curr
     * @param key
     * @return root node
     */
    private Node deleteHelper(Node curr, T key){
        if (key.compareTo(curr.data) == 0){
            // see target, deleting the target.
            if(curr.left == null && curr.right == null){
                return null; // if target is leaf.
            }else if (curr.left != null && curr.right == null){
                return curr.left; // has only left child
            }else{
                return curr.right; // has only right child
            }
            
        }else if (key.compareTo(curr.data) < 0){
            // target falls to the left subtree.
            curr.left = deleteHelper(curr.left, key);
            if(Math.abs(getNodeHeight(curr.left) - getNodeHeight(curr.right)) > this.maxImbalance){
                // Case: if at current level, the tree is imbalanced, check right subtree's right height & right height
                if (curr.right == null){
                    return curr;
                }
                if (getNodeHeight(curr.right.right) >= getNodeHeight(curr.right.left)){
                    return this.rotateLeft(curr);
                }else{
                    return this.rotateRightLeft(curr);
                }
            }else{ // case: current subtree still AVL balanced
                return curr;
            }
        }else{
            // target falls to the right subtree.
            curr.right = deleteHelper(curr.right, key);
            if(Math.abs(getNodeHeight(curr.left) - getNodeHeight(curr.right)) > this.maxImbalance){
                // Case: if at current level, the tree is imbalanced, check left subtree's left height & right height
                if(curr.left == null){
                    return curr;
                }
                if (getNodeHeight(curr.left.left) >= getNodeHeight(curr.left.right)){
                    return this.rotateRight(curr);
                }else{
                   return this.rotateLeftRight(curr);
                }
            }else{ // Case: if at the level, current subtree still AVL balanced
                return curr;
            }
        }
    }

    /**
     * Method for finding the in order successor
     * @param curr the current node
     * @return the in order successor of current node
     */
    private Node findInOrderSuccessor(Node curr){
        if (curr.right == null){
            return null;
        }else{
            if (curr.right.left == null){
                return curr.right;
            }else{
                Node node_ptr = curr.right.left;
                while (node_ptr.left != null){
                    node_ptr = node_ptr.left;
                }
                return node_ptr;
            }
        }
    }

    /**
     * find the parent of target Node
     * @param target
     * @param key
     * @return target Node
     */
    private Node findParent(Node target, T key){
        if (target.left == null && target.right == null){
            return null;
        }else if (target.left != null && target.right == null){
            if(key.compareTo(target.left.data) == 0){
                return target;
            }else{
                return searchNode(target.left, key);
            }
        }else if (target.left == null && target.right != null){
            if(key.compareTo(target.right.data) == 0){
                return target;
            }else{
                return searchNode(target.right, key);
            }
        }else{
            if(key.compareTo(target.left.data) == 0 || key.compareTo(target.right.data) == 0){
                return target;
            }else if(key.compareTo(target.data) > 0){
                return searchNode(target.right, key);
            }else{
                return searchNode(target.left, key);
            }
        }
    }

    /**
     * <p>Search for key in the tree. Return a reference to it if it's in there,
     * or {@code null} otherwise.</p>
     * @param key The key to search for.
     * @return key if key is in the tree, or {@code null} otherwise.
     * @throws EmptyTreeException if the tree is empty.
     */
    public T search(T key) throws EmptyTreeException {
        if (this.root == null){
            throw new EmptyTreeException("Tree is empty for search()");
        }else{
            return searchKey(this.root, key);
        }
    }

    /**
     * Search method healper
     * @param target
     * @param key
     * @return target node data
     */
    private T searchKey(Node target, T key){
        if (target == null){
            return null;
        }else{
            if (key.compareTo(target.data) == 0){
                return target.data; // directly return if it's the one we are looking for.
            }else if (key.compareTo(target.data) > 0){
                return searchKey(target.right, key);
            }else{
                return searchKey(target.left, key);
            }
        }
    }
    /**
     * Search target Node
     * @param target
     * @param key
     * @return target Node
     */
    private Node searchNode(Node target, T key){
        if (target == null){
            return null;
        }else{
            if(key.compareTo(target.data) == 0){
                return target;
            }else if(key.compareTo(target.data) > 0){
                return searchNode(target.right, key);
            }else{
                return searchNode(target.left, key);
            }
        }
    }

    

    /**
     * Retrieves the maximum imbalance parameter.
     * @return The maximum imbalance parameter provided as a constructor parameter.
     */
    public int getMaxImbalance(){
        return this.maxImbalance;
    }


    /**
     * <p>Return the height of the tree. The height of the tree is defined as the length of the
     * longest path between the root and the leaf level. By definition of path length, a
     * stub tree has a height of 0, and we define an empty tree to have a height of -1.</p>
     * @return The height of the tree. If the tree is empty, returns -1.
     */
    public int getHeight() {
        if (isEmpty()){
            return -1;
        }else{
            return this.getNodeHeight(this.root);
        }
    }

    /**
     * Query the tree for emptiness. A tree is empty iff it has zero keys stored.
     * @return {@code true} if the tree is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this.size == 0;
    }

    /**
     * Return the key at the tree's root node.
     * @return The key at the tree's root node.
     * @throws  EmptyTreeException if the tree is empty.
     */
    public T getRoot() throws EmptyTreeException{
        if (isEmpty()){
            throw new EmptyTreeException("Tree is empty for getRoot()");
        }else{
            return this.root.data;
        }
    }


    /**
     * <p>Establishes whether the AVL-G tree <em>globally</em> satisfies the BST condition. This method is
     * <b>terrifically useful for testing!</b></p>
     * @return {@code true} if the tree satisfies the Binary Search Tree property,
     * {@code false} otherwise.
     */
    public boolean isBST() {
        return isBSTHelper(this.root);
    }

    /**
     * helper method for determine if the tree is a BST.
     * Based on: left child is always lesser than currNode, right child is always greater than the currNode.
     * @param target
     * @return
     */
    private boolean isBSTHelper(Node target){
        if (target == null){
            return true;
        }else {
            if (target.left == null && target.right == null){
                return true;
            }else if(target.left == null && target.right != null){
                if ((target.right.data).compareTo(target.data) > 0){
                    return isBSTHelper(target.right);
                }
            }else if (target.left != null && target.right == null){
                if ((target.left.data).compareTo(target.data) < 0){
                    return isBSTHelper(target.left);
                }
            }else{
                if (((target.right.data).compareTo(target.data) > 0) && ((target.left.data).compareTo(target.data) < 0)){
                    return isBSTHelper(target.right) && isBSTHelper(target.left);
                }
            }
            return false;
        }
    }


    /**
     * <p>Establishes whether the AVL-G tree <em>globally</em> satisfies the AVL-G condition. This method is
     * <b>terrifically useful for testing!</b></p>
     * @return {@code true} if the tree satisfies the balance requirements of an AVLG tree, {@code false}
     * otherwise.
     */
    public boolean isAVLGBalanced() {
        return AVLBalancedHelper(this.root);       // ERASE THIS LINE AFTER YOU IMPLEMENT THIS METHOD!
    }


    /*
    *   private method to find the height of the current node.
    */
    private int getHeightDiff(Node curr){
        if (curr.left == null && curr.right == null){
            return 0;
        }else if (curr.left != null && curr.right == null){
            return (getNodeHeight(curr.left) + 1);
        }else if (curr.left == null && curr.right != null){
            return (-1 - getNodeHeight(curr.right));
        }else{
            return Math.abs(getNodeHeight(curr.left) - getNodeHeight(curr.right));
        }
    }

    /*
    *   isAVLBalanced() method helper.
    */
    private boolean AVLBalancedHelper(Node curr){
        if (curr == null){
            return true;
        }else{
            if (getHeightDiff(curr) > this.maxImbalance){
                return false; // directly return false when the invariant of AVL is violated.
            }else{
                return AVLBalancedHelper(curr.left) && AVLBalancedHelper(curr.right);
            }
           // return height_diff <= this.maxImbalance && AVLBalancedHelper(curr.left) && AVLBalancedHelper(curr.right);
        }
    }

    /**
     * <p>Empties the AVL-G Tree of all its elements. After a call to this method, the
     * tree should have <b>0</b> elements.</p>
     */
    public void clear(){
        if(this.root != null){
            this.root.right = null;
            this.root.left = null;
            this.root = null;
        }
        this.size = 0;
    }


    /**
     * <p>Return the number of elements in the tree.</p>
     * @return  The number of elements in the tree.
     */
    public int getCount(){
        return this.size;
    }

    /**************************************************************
     **************************************************************
     ********************* Debugging Methods  *********************
     *************************************************************/
    /**
     * for debugging use only: get root node
     * @return the root Node
     */
    public Node getRootNode(){
        return this.root;
    }
    /**
     * for debugging use only: get left child
     * @param curr
     * @return Node 
     */
    public Node getLeft(Node curr){
        return curr.left;
    }
    /**
     * for debugging use only: get right child
     * @param curr
     * @return Node
     */
    public Node getRight(Node curr){
        return curr.right;
    }
    /**
     * for debugging use only: get current node data
     * @param curr
     * @return T
     */
    public T getData(Node curr){
        if(curr == null){
            return null;
        }else{
            return curr.data;
        }
        
    }
    /**
     *  for debugging use only: print tree
     */
    public void printTree(Node curr){
        if (curr == null){
            return;
        }
        if (curr.left != null && curr.right == null){
            System.out.println(curr.data + " =>" +" Left : " + curr.left.data + " Right : Null");
        }else if(curr.left == null && curr.right != null){
            System.out.println(curr.data + " =>" + " Left : Null " + " Right : " + curr.right.data);
        }else if(curr.left == null && curr.right == null){
            System.out.println(curr.data + " =>" +" Left : NULL " + " Right : NULL");
        }else{
            System.out.println(curr.data + " =>" + " Left : " + curr.left.data + " Right : " + curr.right.data);         
        }
        printTree(curr.left);
        printTree(curr.right);
    }
}
