package simpledb;

import java.util.*;

/**
 * The Join operator implements the relational join operation.
 */
// 连接也是运算符的一种(谓语, 迭代器1, 迭代器2)
public class Join extends Operator {

    private static final long serialVersionUID = 1L;

    private JoinPredicate p;
    private DbIterator child1;
    private DbIterator child2;
    private TupleDesc td;
    private Tuple tuple1;
    private Tuple tuple2;
    /**
     * Constructor. Accepts to children to join and the predicate to join them
     * on
     *
     * @param p
     *            The predicate to use to join the children
     * @param child1
     *            Iterator for the left(outer) relation to join
     * @param child2
     *            Iterator for the right(inner) relation to join
     */
    public Join(JoinPredicate p, DbIterator child1, DbIterator child2) {
        // some code goes here
        this.p = p;
        this.child1 = child1;
        this.child2 = child2;
        this.td = TupleDesc.merge(child1.getTupleDesc(), child2.getTupleDesc());
    }

    public JoinPredicate getJoinPredicate() {
        // some code goes here
        return this.p;
    }

    /**
     * @return
     *       the field name of join field1. Should be quantified by
     *       alias or table name.
     * */
    public String getJoinField1Name() {
        // some code goes here
        return this.child1.getTupleDesc().getFieldName(this.p.getField1());
    }

    /**
     * @return
     *       the field name of join field2. Should be quantified by
     *       alias or table name.
     * */
    public String getJoinField2Name() {
        // some code goes here
        return this.child2.getTupleDesc().getFieldName(this.p.getField2());
    }

    /**
     * @see simpledb.TupleDesc#merge(TupleDesc, TupleDesc) for possible
     *      implementation logic.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.td;
    }

    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // some code goes here
        super.open();
        this.child1.open();
        this.child2.open();
    }

    public void close() {
        // some code goes here
        super.close();
        this.child1.close();
        this.child2.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        this.child1.rewind();
        this.child2.rewind();
    }

    /**
     * Returns the next tuple generated by the join, or null if there are no
     * more tuples. Logically, this is the next tuple in r1 cross r2 that
     * satisfies the join predicate. There are many possible implementations;
     * the simplest is a nested loops join.
     * <p>
     * Note that the tuples returned from this particular implementation of Join
     * are simply the concatenation of joining tuples from the left and right
     * relation. Therefore, if an equality predicate is used there will be two
     * copies of the join attribute in the results. (Removing such duplicate
     * columns can be done with an additional projection operator if needed.)
     * <p>
     * For example, if one tuple is {1,2,3} and the other tuple is {1,5,6},
     * joined on equality of the first column, then this returns {1,2,3,1,5,6}.
     *
     * @return The next matching tuple.
     * @see JoinPredicate#filter
     */
    // 有三种Join算法, NLJ(Nested Loop Join), Hash Join, Sort-Merge Join
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        if (child1.hasNext() && tuple1 == null) {
            tuple1 = child1.next();
        }
        while (tuple1 != null) {
            while (child2.hasNext()) {
                tuple2 = child2.next();
                if (p.filter(tuple1, tuple2)) {
                    Tuple tuple = new Tuple(this.td);
                    Iterator<Field> field1 = tuple1.Fields();
                    Iterator<Field> field2 = tuple2.Fields();
                    int index = 0;
                    while (field1.hasNext()) {
                        tuple.setField(index, field1.next());
                        index++;
                    }
                    while (field2.hasNext()) {
                        tuple.setField(index, field2.next());
                        index++;
                    }
                    return tuple;
                }
            }
            if (child1.hasNext()) {
                tuple1 = child1.next();
            } else {
                tuple1 = null;
            }
            child2.rewind();
        }
        return null;
    }


    @Override
    public DbIterator[] getChildren() {
        // some code goes here
        return new DbIterator[]{this.child1, this.child2};
    }

    @Override
    public void setChildren(DbIterator[] children) {
        // some code goes here
        this.child1 = children[0];
        this.child2 = children[1];
    }

}
