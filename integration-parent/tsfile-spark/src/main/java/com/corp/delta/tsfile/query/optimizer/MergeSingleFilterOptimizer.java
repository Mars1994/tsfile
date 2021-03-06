package com.corp.delta.tsfile.query.optimizer;

import com.corp.delta.tsfile.query.common.FilterOperator;
import com.corp.delta.tsfile.query.exception.MergeFilterException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MergeSingleFilterOptimizer implements IFilterOptimizer {

    @Override
    public FilterOperator optimize(FilterOperator filter) throws MergeFilterException {
        mergeSamePathFilter(filter);

        return filter;
    }

    private String mergeSamePathFilter(FilterOperator filter) throws MergeFilterException {
        if (filter.isLeaf())
            return filter.getSinglePath();
        List<FilterOperator> children = filter.getChildren();
        if (children.isEmpty()) {
            throw new MergeFilterException("this inner filter has no children!");
        }
        if (children.size() == 1) {
            throw new MergeFilterException("this inner filter has just one child!");
        }
        String childPath = mergeSamePathFilter(children.get(0));
        String tempPath;
        for (int i = 1; i < children.size(); i++) {
            tempPath = mergeSamePathFilter(children.get(i));
            // if one of children differs from others or is not single node(path = null), filter's path
            // is null
            if (tempPath == null || !tempPath.equals(childPath))
                childPath = null;
        }
        if (childPath != null) {
            filter.setIsSingle(true);
            filter.setSinglePath(childPath);
            return childPath;
        }

        // make same paths close
        Collections.sort(children);
        List<FilterOperator> ret = new ArrayList<>();

        List<FilterOperator> tempExtrNode = null;
        int i;
        for (i = 0; i < children.size(); i++) {
            tempPath = children.get(i).getSinglePath();
            // sorted by path, all "null" paths are in the end
            if (tempPath == null) {
                break;
            }
            if (childPath == null) {
                // first child to be added
                childPath = tempPath;
                tempExtrNode = new ArrayList<>();
                tempExtrNode.add(children.get(i));
            } else if (childPath.equals(tempPath)) {
                // successive next single child with same path,merge it with previous children
                tempExtrNode.add(children.get(i));
            } else {
                // not more same, add exist nodes in tempExtrNode into a new node
                // prevent make a node which has only one child.
                if (tempExtrNode.size() == 1) {
                    ret.add(tempExtrNode.get(0));
                    // use exist Object directly for efficiency
                    tempExtrNode.set(0, children.get(i));
                    childPath = tempPath;
                } else {
                    // add a new inner node
                    FilterOperator newFilter = new FilterOperator(filter.getTokenIntType(), true);
                    newFilter.setSinglePath(childPath);
                    newFilter.setChildrenList(tempExtrNode);
                    ret.add(newFilter);
                    tempExtrNode = new ArrayList<>();
                    tempExtrNode.add(children.get(i));
                    childPath = tempPath;
                }
            }
        }
        // the last several children before "not single paths" has not been added to ret list.
        if (childPath != null) {
            if (tempExtrNode.size() == 1) {
                ret.add(tempExtrNode.get(0));
            } else {
                // add a new inner node
                FilterOperator newFil = new FilterOperator(filter.getTokenIntType(), true);
                newFil.setSinglePath(childPath);
                newFil.setChildrenList(tempExtrNode);
                ret.add(newFil);
            }
        }
        // add last null children
        for (; i < children.size(); i++) {
            ret.add(children.get(i));
        }
        if (ret.size() == 1) {
            // all children have same path, which means this filter node is a single node
            filter.setIsSingle(true);
            filter.setSinglePath(childPath);
            filter.setChildrenList(ret.get(0).getChildren());
            return childPath;
        } else {
            filter.setIsSingle(false);
            filter.setChildrenList(ret);
            return null;
        }
    }
}
