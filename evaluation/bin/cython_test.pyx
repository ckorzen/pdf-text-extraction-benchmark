#!/usr/bin/python
import time
import sys
import operator

# The Trie data structure keeps a set of items, organized with one node for
# each letter. Each node has a branch for each letter that may follow it in the
# set of items.
class TrieNode:
    cdef __init__(self, str items[]=[]):
        self.children_nodes = {}
        # All items of the children nodes.
        self.items = {}
        # The item of this node.
        self.item = None
        cdef str item
        for item in items:
            self.insert(item)

    cdef insert( self, str item ):
        '''
        Insert the given item into this node.
        '''
        node = self
                        
        # Traverse the tree to find the position where to insert the item.
        cdef str letter
        for letter in item:
            if letter not in node.children_nodes: 
                node.children_nodes[letter] = TrieNode()
            node = node.children_nodes[letter]
        # Keep track of the frequency of the item in this node.
        if node.item is not None:
            self.items[item] += 1
        else:
            node.item = item
            self.items[item] = 1

    cdef delete( self, str item ):
        '''
        Deletes the given item from this node.
        '''
        cdef TrieNode = self
        # Traverse the tree to check, if this node contains the item.
        cdef str letter
        for letter in item:
            if letter in node.children_nodes: 
                node = node.children_nodes[letter]
            else:
                return
        if node.item is not None:
            self.items[item] -= 1
            if self.items[item] == 0:
                # Set the item to None to mark this node as deleted.
                node.item = None;
                del self.items[item]

    # Returns the best match of all items that are less than the given
    # maximum distance from the target item
    def best_match(self, item, maxCost):
       results = self.search(item, maxCost)
       results.sort(key=operator.itemgetter(1))
       
       if (len(results) > 0):
           return results[0][0]
       else:
           return None  

    # The search function returns a list of all items that are less than the given
    # maximum distance from the target item
    def search(self, item, maxCost ):

       # build first row
       currentRow = range( len(item) + 1 )

       results = []

       # recursively search each branch of the trie
       for letter in self.children_nodes:
           self.searchRecursive( self.children_nodes[letter], letter, item, currentRow, 
            results, maxCost )

       return results

    # This recursive helper is used by the search function above. It assumes that
    # the previousRow has been filled in already.
    def searchRecursive(self, node, letter, item, previousRow, results, maxCost ):

        columns = len( item ) + 1
        currentRow = [ previousRow[0] + 1 ]

        # Build one row for the letter, with a column for each letter in the target
        # item, plus one for the empty string at column 0
        for column in range( 1, columns ):

            insertCost = currentRow[column - 1] + 1
            deleteCost = previousRow[column] + 1

            if item[column - 1] != letter:
                replaceCost = previousRow[ column - 1 ] + 1
            else:                
                replaceCost = previousRow[ column - 1 ]

            currentRow.append( min( insertCost, deleteCost, replaceCost ) )

        # if the last entry in the row indicates the optimal cost is less than the
        # maximum cost, and there is a item in this trie node, then add it.
        if currentRow[-1] <= maxCost and node.item != None:
            results.append( (node.item, currentRow[-1] ) )

        # if any entries in the row are less than the maximum cost, then 
        # recursively search each branch of the trie
        if min( currentRow ) <= maxCost:
            for letter in node.children_nodes:
                self.searchRecursive( node.children_nodes[letter], letter, item, currentRow, 
                    results, maxCost )