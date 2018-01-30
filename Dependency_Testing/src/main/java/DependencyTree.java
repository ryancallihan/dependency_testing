//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

//package edu.stanford.nlp.parser.nndep;

import java.util.ArrayList;
import java.util.List;

class DependencyTree {
    int n;
    List<Integer> head;
    List<String> label;
    private int counter;

    public DependencyTree() {
        this.n = 0;
        this.head = new ArrayList();
        this.head.add(-1);
        this.label = new ArrayList();
        this.label.add("-UNKNOWN-");
    }

    public DependencyTree(DependencyTree tree) {
        this.n = tree.n;
        this.head = new ArrayList(tree.head);
        this.label = new ArrayList(tree.label);
    }

    public void add(int h, String l) {
        ++this.n;
        this.head.add(h);
        this.label.add(l);
    }

    public void set(int k, int h, String l) {
        this.head.set(k, h);
        this.label.set(k, l);
    }

    public int getHead(int k) {
        return k > 0 && k <= this.n ? (Integer)this.head.get(k) : -1;
    }

    public String getLabel(int k) {
        return k > 0 && k <= this.n ? (String)this.label.get(k) : "-NULL-";
    }

    public int getRoot() {
        for(int k = 1; k <= this.n; ++k) {
            if (this.getHead(k) == 0) {
                return k;
            }
        }

        return 0;
    }

    public boolean isSingleRoot() {
        int roots = 0;

        for(int k = 1; k <= this.n; ++k) {
            if (this.getHead(k) == 0) {
                ++roots;
            }
        }

        return roots == 1;
    }

    public boolean isTree() {
        List<Integer> h = new ArrayList();
        h.add(-1);

        int i;
        for(i = 1; i <= this.n; ++i) {
            if (this.getHead(i) < 0 || this.getHead(i) > this.n) {
                return false;
            }

            h.add(-1);
        }

        for(i = 1; i <= this.n; ++i) {
            for(int k = i; k > 0 && ((Integer)h.get(k) < 0 || (Integer)h.get(k) >= i); k = this.getHead(k)) {
                if ((Integer)h.get(k) == i) {
                    return false;
                }

                h.set(k, i);
            }
        }

        return true;
    }

    public boolean isProjective() {
        if (!this.isTree()) {
            return false;
        } else {
            this.counter = -1;
            return this.visitTree(0);
        }
    }

    private boolean visitTree(int w) {
        int i;
        for(i = 1; i < w; ++i) {
            if (this.getHead(i) == w && !this.visitTree(i)) {
                return false;
            }
        }

        ++this.counter;
        if (w != this.counter) {
            return false;
        } else {
            for(i = w + 1; i <= this.n; ++i) {
                if (this.getHead(i) == w && !this.visitTree(i)) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean equal(DependencyTree t) {
        if (t.n != this.n) {
            return false;
        } else {
            for(int i = 1; i <= this.n; ++i) {
                if (this.getHead(i) != t.getHead(i)) {
                    return false;
                }

                if (!this.getLabel(i).equals(t.getLabel(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public void print() {
        for(int i = 1; i <= this.n; ++i) {
            System.out.println(i + " " + this.getHead(i) + " " + this.getLabel(i));
        }

        System.out.println();
    }
}
