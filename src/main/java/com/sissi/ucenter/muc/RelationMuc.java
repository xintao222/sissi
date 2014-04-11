package com.sissi.ucenter.muc;

import com.sissi.ucenter.Relation;

/**
 * @author kim 2014年2月11日
 */
public interface RelationMuc extends Relation {

	public RelationMuc role(String role);

	public RelationMuc role(String role, boolean force);

	public RelationMuc affiliation(String affiliation);

	public RelationMuc affiliation(String affiliation, boolean force);

	public String role();

	public String resource();

	public String affiliation();

	public boolean outcast();

	public boolean name(String name, boolean allowNull);

	public RelationMuc noneRole();
}
