package api.data;

import api.perms.Group;

/**
 * Created by loucass003 on 2/7/17.
 */
public interface IUserData
{
	int getFuturyCoins();
	void setFuturyCoins(int fc);

	int getTurfuryCoins();
	void setTurfuryCoins(int tc);

	Group getGroup();
	void setGroup(Group g);
}
