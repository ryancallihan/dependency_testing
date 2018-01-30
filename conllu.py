#!/usr/bin/env python3

# for loading and saving conllu files.

import codecs
cols = 'id', 'form', 'lemma', 'upostag', 'xpostag', 'feats', 'head', 'deprel', 'deps', 'misc'


class Sent(object):
    """a conllu sentence.
    dumb: dummy value for the pseudo root node
    cols: names of the 10 columns in the conllu format
    id: tuple<int>
    head: tuple<int|str>
    form, lemma, upostag, xpostag, feats, deprel, deps, misc: tuple<str>
    all 10 tuples are of equal length.
    multi: multi-word tokens.
    """

    __slots__ = cols + ('multi', 'sentence')
    cols = cols
    dumb = ""


    def __init__(self, lines):
        """lines: iter<str>"""

        self.sentence = ''
        dumb = self.dumb
        multi = []
        nodes = [[0, dumb, dumb, dumb, dumb, dumb, dumb, dumb, dumb, dumb]]
        for line in lines:
            node = line.split("\t")
            assert 10 == len(node)
            try:
                node[0] = int(node[0])
            except ValueError:
                if "-" in node[0]: multi.append(line)
            else:
                try:  # head might be empty for interim results
                    node[6] = int(node[6])
                except ValueError:
                    pass
                nodes.append(node)
        self.multi = tuple(multi)
        for attr, val in zip(self.cols, zip(*nodes)):
            setattr(self, attr, val)
        self.getSentence()

    def __eq__(self, other):
        for attr in self.__slots__:
            if ((not hasattr(other, attr))
                    or (getattr(self, attr) != getattr(other, attr))):
                return False
        else:
            return True

    def getSentence(self):
        for i, f in enumerate(self.form):
            self.sentence += f
            if self.misc[i] != 'SpaceAfter=No':
                self.sentence += ' '



del cols


def load_text(file_path):
    """-> iter<Sent>"""
    with open(file_path, "r", encoding="utf-8") as file:
        sent = []
        for line in file:
            line = line.strip()
            if line.startswith("# text = "):
                yield line.replace("# text = ", "")


def load(file_path):
    """-> iter<Sent>"""
    with open(file_path, "r", encoding="utf-8") as file:
        sent = []
        for line in file:
            line = line.strip()
            if line.startswith("#"):
                pass
            elif line:
                sent.append(line.replace(" ", "\xa0"))
            elif sent:
                yield Sent(sent)
                sent = []
        if sent: yield Sent(sent)


def save_text(sents, file_path):
    """sents: iter<Sent>"""
    with open(file_path, 'a', encoding="utf-8") as file:
        for sent in sents:
            file.write(sent + "\n")
        file.close()


def save(sents, file_path):
    """sents: iter<Sent>"""
    with open(file_path, 'w', encoding="utf-8") as file:
        for sent in sents:
            multi_idx = [int(multi[:multi.index("-")]) for multi in sent.multi]
            w, m = 1, 0
            while w < len(sent.id):
                if m < len(multi_idx) and w == multi_idx[m]:
                    line = sent.multi[m]
                    m += 1
                else:
                    line = "\t".join([str(getattr(sent, col)[w]) for col in sent.cols])
                    w += 1
                file.write(line.replace("\xa0", " "))
                file.write("\n")
            file.write("\n")

def get_all_dependencies(sents):
    deps = set()
    for sent in sents:
        for d in sent.deprel:
            deps.add(d)
    return deps

if '__main__' == __name__:
    import sys

    try:
        path, temp = sys.argv[1:]
    except ValueError:
        sys.exit("usage: {} conllu_path temp_path".format(sys.argv[0]))

    # sents = list(load(path))
    # save(sents, temp)
    # assert sents == list(load(temp))
    # sents = list(load_text(path))
    # save_text(sents, temp)

    sents = list(load('bnc.conll'))
    deps = get_all_dependencies(sents)


    print("alright.")

    with open('bnc.txt', 'w', encoding='utf-8') as f:
        for sent in sents:
            f.write(sent.sentence + '\n')
        f.close()
