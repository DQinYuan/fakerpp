package org.testany.fakerpp.core.util.earclipping;

/**
 * 双向链表的实现
 * @param <T>
 */
public final class DoublyLinkedList<T> {

    public static final class Node<T> {

        private T mValue;
        private Node<T> mNext;
        private Node<T> mPrevious;

        public T getValue() {
            return mValue;
        }

        public Node<T> getNext() {
            return mNext;
        }

        public Node<T> getPrevious() {
            return mPrevious;
        }
    }

    private int mCount = 0;
    private Node<T> mHead;
    private Node<T> mTail;

    public DoublyLinkedList() {
    }

    public int getCount() {
        return mCount;
    }

    public Node<T> getFirst() {
        return mHead;
    }

    public Node<T> getLast() {
        return mTail;
    }

    public void addToLast(T value) {
        final Node<T> node = new Node<>();
        node.mValue = value;

        mCount += 1;
        if (mHead == null) {
            mHead = node;
            mTail = node;
        } else {
            final Node<T> currentLastNode = mTail;
            currentLastNode.mNext = node;
            node.mPrevious = currentLastNode;
            mTail = node;
        }
    }

    public void addToFirst(T value) {
        final Node<T> node = new Node<>();
        node.mValue = value;

        mCount += 1;
        if (mHead == null) {
            mHead = node;
            mTail = node;
        } else {
            final Node<T> currentFirstNode = mHead;
            currentFirstNode.mPrevious = node;
            node.mNext = currentFirstNode;
            mHead = node;
        }
    }

    public Node<T> addAfter(Node<T> node, T value) {
        if (mHead != null) {
            Node<T> insertNode = new Node<>();
            insertNode.mValue = value;

            Node<T> currentNode = mHead;
            while (currentNode != null) {
                if (currentNode == node) {
                    // Insert after this node
                    Node<T> nextNode = currentNode.mNext;

                    currentNode.mNext = insertNode;
                    insertNode.mPrevious = currentNode;
                    insertNode.mNext = nextNode;
                    nextNode.mPrevious = insertNode;

                    mCount += 1;
                    return insertNode;
                } else {
                    currentNode = currentNode.mNext;
                }
            }
        }

        return null;
    }

    public void remove(Node<T> removeNode) {
        final Node<T> nextNode = removeNode.mNext;
        final Node<T> previousNode = removeNode.mPrevious;

        if (previousNode != null) {
            previousNode.mNext = nextNode;
        }
        if (nextNode != null) {
            nextNode.mPrevious = previousNode;
        }
        if (mHead == removeNode) {
            mHead = nextNode;
        }
        if (mTail == removeNode) {
            mTail = previousNode;
        }

        mCount -= 1;
    }
}