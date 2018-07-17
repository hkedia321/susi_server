/**
 *  GetSkillMetadataService
 *  Copyright 1.08.2017 by Saurabh Jain , @saurabhjn76
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

package ai.susi.server.api.cms;

import ai.susi.DAO;
import ai.susi.json.JsonObjectWithDefault;
import ai.susi.mind.SusiSkill;
import ai.susi.server.*;
import org.json.JSONObject;
import ai.susi.json.JsonTray;
import org.json.JSONArray;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 This Servlet gives a API Endpoint to list meta for a  Skill. Given its model, group and language and skill.
 Can be tested on http://127.0.0.1:4000/cms/getSkillMetadata.json?model=general&group=Knowledge&language=en&skill=creator_info
 */
public class GetSkillMetadataService extends AbstractAPIHandler implements APIHandler {


    private static final long serialVersionUID = 3446536703362688060L;

    @Override
    public UserRole getMinimalUserRole() { return UserRole.ANONYMOUS; }

    @Override
    public JSONObject getDefaultPermissions(UserRole baseUserRole) {
        return null;
    }

    @Override
    public String getAPIPath() {
        return "/cms/getSkillMetadata.json";
    }

    @Override
    public ServiceResponse serviceImpl(Query call, HttpServletResponse response, Authorization rights, final JsonObjectWithDefault permissions) {

        try {
            DAO.susi.observe(); // get a database update
        } catch (IOException e) {
            DAO.log(e.getMessage());
        }
        JSONObject json = new JSONObject(true);
        json.put("accepted", false);
        String model = call.get("model", "");
        String group = call.get("group", "");
        String language = call.get("language", "");
        String skillname = call.get("skill", "");
        String privateSkill = call.get("private", null);
        String userId = call.get("userid", "");

        if ( (model.length() == 0 && privateSkill == null) || group.length() == 0 || language.length() == 0 || skillname.length() == 0 ) { 
            json.put("message", "Error: Bad parameter call");
            return new ServiceResponse(json);
        }

        JSONObject skillMetadata = new JSONObject(true);
        if (privateSkill == null) {
            skillMetadata = SusiSkill.getSkillMetadata(model, group, language, skillname);
        }
        else {
            JsonTray chatbot = DAO.chatbot;
            if (chatbot.has(userId)) {
                JSONArray userChatBots = new JSONArray();
                userChatBots = chatbot.getJSONObject(userId).getJSONArray("chatbots");
                json.put("skill_meta",userChatBots);
            }
            else {
                json.put("message", "Error: The user doesn't have any bots");
            }
        }

        json.put("skill_metadata", skillMetadata);
        json.put("accepted", true);
        json.put("message", "Success: Fetched Skill's Metadata");
        return new ServiceResponse(json);
    }

}
