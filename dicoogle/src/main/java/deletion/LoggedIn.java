/**
 * Copyright (C) 2014  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
 *
 * This file is part of Dicoogle/dicoogle.
 *
 * Dicoogle/dicoogle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dicoogle/dicoogle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dicoogle.  If not, see <http://www.gnu.org/licenses/>.
 */
package deletion;

/**
 * Maintains information about a users login.
 *
 * @author António Novo <antonio.novo@ua.pt>
 */
public class LoggedIn
{
	private String userName;
	private boolean admin;

	public LoggedIn(String userName, boolean isAdmin)
	{
		this.userName = userName;
		this.admin = isAdmin;
	}

	/**
	 * @return the user name
	 */
	public String getUserName()
	{
		return userName;
	}

	/**
	 * @return if this user is admin
	 */
	public boolean isAdmin()
	{
		return admin;
	}
}