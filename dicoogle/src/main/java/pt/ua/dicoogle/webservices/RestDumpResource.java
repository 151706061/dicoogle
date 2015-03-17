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
package pt.ua.dicoogle.webservices;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml3;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.utils.DictionaryAccess;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.task.JointQueryTask;
import pt.ua.dicoogle.sdk.task.Task;

/**
 *
 * @author fmvalente
 */


//TODO:add type to file search
public class RestDumpResource extends ServerResource{
        
    @Get
    public String represent(){
                
        String SOPInstanceUID = getRequest().getResourceRef().getQueryAsForm().getValues("uid");
        if(SOPInstanceUID == null) return null;
        
        DictionaryAccess da = DictionaryAccess.getInstance() ; 
        
        String query = "SOPInstanceUID:"+SOPInstanceUID;
        
        
        HashMap<String, String> extraFields = new HashMap<String, String>();
        
        for (String s : da.getTagList().keySet()) {
                extraFields.put(s, s);
        }

        //performs the search
        Iterable<SearchResult> queryResultList = null;
        try {
            System.out.println("Query: + " + query);
            queryResultList = PluginController.get().queryDispatch("lucene", query, extraFields).get();
        }
        catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(RestDumpResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<tags>\n");
        
        for(SearchResult r : queryResultList){
            for (String s : r.getExtraData().keySet()){
                System.out.println(s);
                sb.append("\t<tag name=\"").append(s).append("\">").append(escapeHtml3(((String)r.getExtraData().get(s)).trim())).append("</tag>\n");
            }
        }

        sb.append("</tags>");
        
        //and returns an xml version of our dim search
        return sb.toString();
        
    }
}
