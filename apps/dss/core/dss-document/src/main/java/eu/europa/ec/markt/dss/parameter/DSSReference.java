/*
 * DSS - Digital Signature Services
 *
 * Copyright (C) 2013 European Commission, Directorate-General Internal Market and Services (DG MARKT), B-1049 Bruxelles/Brussel
 *
 * Developed by: 2013 ARHS Developments S.A. (rue Nicolas Bové 2B, L-1253 Luxembourg) http://www.arhs-developments.com
 *
 * This file is part of the "DSS - Digital Signature Services" project.
 *
 * "DSS - Digital Signature Services" is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * DSS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * "DSS - Digital Signature Services".  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.markt.dss.parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 * <p/>
 * <p> DISCLAIMER: Project owner DG-MARKT.
 *
 * @author <a href="mailto:dgmarkt.Project-DSS@arhs-developments.com">ARHS Developments</a>
 * @version $Revision: 1016 $ - $Date: 2011-06-17 15:30:45 +0200 (Fri, 17 Jun 2011) $
 */
public class DSSReference {

	private String id;
	private String uri;
	private String type;

	private String digestMethod;

	private List<DSSTransform> transforms;

	/**
	 * The default constructor
	 */
	public DSSReference() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDigestMethod() {
		return digestMethod;
	}

	public void setDigestMethod(String digestMethod) {
		this.digestMethod = digestMethod;
	}

	public List<DSSTransform> getTransforms() {
		return transforms;
	}

	public void setTransforms(List<DSSTransform> transforms) {
		this.transforms = transforms;
	}

	@Override
	public String toString() {
		return "DSSReference{" +
			  "id='" + id + '\'' +
			  ", uri='" + uri + '\'' +
			  ", type='" + type + '\'' +
			  ", digestMethod='" + digestMethod + '\'' +
			  ", transforms=" + transforms +
			  '}';
	}
}
