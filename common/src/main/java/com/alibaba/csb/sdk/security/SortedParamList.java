package com.alibaba.csb.sdk.security;

import java.util.ArrayList;
import java.util.Collection;

public class SortedParamList extends ArrayList<ParamNode> {

	private static final long serialVersionUID = 1L;

	public boolean add(ParamNode e) {
		if (size() == 0) {
			return super.add(e);
		}
		else {
			return binaryAdd(e, 0, size() - 1);
		}
	}
	
	private boolean binaryAdd(ParamNode e, int start, int end) {
		if (start > end) {
			super.add(start, e);
			return true;
		}
		else if (start == end) {
			if (e.compareTo(get(start)) <= 0) {
				super.add(start, e);
			}
			else {
				super.add(start + 1, e);
			}
			return true;
		}
		else {
			int mid = (start + end) / 2;
			int cmp = e.compareTo(get(mid));
			if (cmp < 0) {
				return binaryAdd(e, start, mid - 1);
			}
			else if (cmp > 0) {
				return binaryAdd(e, mid + 1, end);
			}
			else {
				super.add(mid, e);
				return true;
			}
		}
	}
	
	public boolean addAll(Collection<? extends ParamNode> c) {
		for (ParamNode e : c) {
			add(e);
		}
		return true;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (size() > 0) {
			sb.append(get(0).toString());
		}
		for (int i = 1; i < size(); i++) {
			sb.append("&" + get(i).toString());
		}
		return sb.toString();
	}
	
	public String toRawString() {
		StringBuilder sb = new StringBuilder();
		for (ParamNode e : this) {
			sb.append(e.toRawString());
		}
		return sb.toString();
	}
}
