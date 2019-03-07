/**
 * 
 */
package kevsn.util.db;

import java.sql.SQLException;

/**
 * @author kevin
 *
 */
public class QueryException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2558577876583984321L;

	public QueryException(SQLException e) {
		super(e);
	}

}
