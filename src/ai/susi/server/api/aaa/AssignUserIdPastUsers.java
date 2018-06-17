/**
 *  AssignUserIdPastUsers
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */


package ai.susi.server.api.aaa;

import ai.susi.DAO;
import ai.susi.json.JsonObjectWithDefault;
import ai.susi.server.*;
import ai.susi.json.JsonTray;
import java.util.Collection;
import org.json.JSONObject;
import java.util.UUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Harshit on 17/6/18.
 * Servlet to write user id to past users
 * test locally at http://127.0.0.1:4000/aaa/assignUserId.json
 */
public class AssignUserIdPastUsers extends AbstractAPIHandler implements APIHandler {

  private static final long serialVersionUID = -7418883159709458190L;

  @Override
  public String getAPIPath() {
    return "/aaa/assignUserId.json";
  }

  @Override
  public UserRole getMinimalUserRole() {
    return UserRole.ANONYMOUS;
  }

  @Override
  public JSONObject getDefaultPermissions(UserRole baseUserRole) {
    return null;
  }

  @Override
  public ServiceResponse serviceImpl(Query query, HttpServletResponse response, Authorization rights, JsonObjectWithDefault permissions) throws APIException {

   Collection<ClientIdentity> authorized = DAO.getAuthorizedClients();

   List<JSONObject> userList = new ArrayList<JSONObject>();
   for (Client client : authorized) {
    JSONObject json = client.toJSON();

    ClientIdentity identity = new ClientIdentity(ClientIdentity.Type.email, client.getName());

                //generate client credentials 
    ClientCredential clientCredential = new ClientCredential(ClientCredential.Type.passwd_login, identity.getName());
    Authentication authentication = DAO.getAuthentication(clientCredential);
    String previous_id = authentication.getString("id","");
    if(previous_id != ""){
      String uniqueID = UUID.randomUUID().toString().replace("-", "");
      json.put("prevId",authentication.getString("id",""));

      authentication.put("email",identity.getName());
      authentication.remove("id");
      authentication.put("id","id:"+uniqueID);

      json.put("newId",authentication.getString("id",""));
    userList.add(json);
    }
    
  }
  JSONObject result = new JSONObject(true);
  result.put("users",userList);
  result.put("accepted", true);
  result.put("message", "You successfully changed ids of your users!");
  return new ServiceResponse(result);
}
}
