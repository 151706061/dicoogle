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

/**
 * Class SearchResult is responsible to get results from Lucene indexer But no
 * text results are returned instead we use names of DICOM file and instance a
 * new DicomObject, so in theory this is an abstract to a list of DicomObject,
 * neither list of *names* of DICOM file.
 */
package pt.ua.dicoogle.server;


import pt.ua.dicoogle.core.ServerSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;

import pt.ua.dicoogle.core.dim.DIMGeneric;
import pt.ua.dicoogle.core.dim.Patient;
import pt.ua.dicoogle.core.dim.Serie;
import pt.ua.dicoogle.core.dim.Study;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;

/**
 *
 * @author Luís A. Bastião Silva <bastiao@ua.pt>
 * @since 17 Fev 2009
 */
public class SearchDicomResult implements Iterator<DicomObject> {

    public enum QUERYLEVEL {

        PATIENT, STUDY, SERIE, IMAGE
    }

    private final QUERYLEVEL queryLevel;

    /**
     * Get IndexCore
     */
    Collection<SearchResult> list = null;
    List<Patient> patientList = new ArrayList<>();
    List<Study> studyList = new ArrayList<>();
    List<Serie> seriesList = new ArrayList<>();
    Iterator it = null;

    String currentFile;

    PluginController pluginController;

    public SearchDicomResult(PluginController pController, String searchQuery, boolean isNetwork, ArrayList<String> extrafields, QUERYLEVEL level) {

        pluginController = pController;
        queryLevel = level;

        /**
         * Get the array list of resulst match searchQuery
         */
        System.out.println("QUERY: " + searchQuery);
        System.out.println("QUERYLEVEL: " + queryLevel);
		// TODO: How about search in Network?
        // DebugManager.getInstance().debug(searchQuery);
        // list = core.searchSync(searchQuery, extrafields);

        HashMap<String, String> extraFields = new HashMap<>();
        for (String s : extrafields) {
            extraFields.put(s, s);
        }

        try {
            it = pluginController.queryDispatch("lucene", searchQuery, extraFields).get().iterator();

        } catch (InterruptedException | ExecutionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        it = list.iterator();

        if (level == QUERYLEVEL.PATIENT || level == QUERYLEVEL.STUDY) {
            DIMGeneric dimModel = null;
            try {
                dimModel = new DIMGeneric(list);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            ArrayList<Patient> listPatients = dimModel.getPatients();

            for (Patient p : listPatients) {
                studyList.addAll(p.getStudies());
            }

            it = studyList.iterator();

        } else if (level == QUERYLEVEL.SERIE) {
            DIMGeneric dimModel = null;
            try {
                dimModel = new DIMGeneric(list);
            } catch (Exception ex) {
            }

            ArrayList<Patient> listPatients = dimModel.getPatients();
            for (Patient p : listPatients) {
                studyList.addAll(p.getStudies());
                for (Study s : p.getStudies()) {
                    seriesList.addAll(s.getSeries());
                }
            }
            it = seriesList.iterator();

        }

    }

    @Override
    public boolean hasNext() {
        if (it != null) {
            //DebugManager.getInstance().debug("It has a iterator");
            if (it.hasNext()) {
                //DebugManager.getInstance().debug("and we have a next");
            }
            return it.hasNext();
        } else {
            return false;
        }
    }

    public String getCurrentFile() {
        return this.currentFile;
    }

    @Override
    public DicomObject next() {

        // TODO: this code need to be refactored
        // C-FIND RSP should be builded based on Search Result,
        // instead opening the file to build DicomObject.
        /**
         * Get the fullpath of images
         */
        ServerSettings s = ServerSettings.getInstance();
        String path = s.getPath();

        //DebugManager.getInstance().debug("Path of DICOM: "+path);
        if (it != null && it.hasNext()) {
            Object next = it.next();
            if (queryLevel == QUERYLEVEL.IMAGE) {

                SearchResult sR = (SearchResult) next;

                path = sR.getURI().toString();
                currentFile = path;
                //DebugManager.getInstance().debug("-> Next::: " + next.toString());
                DicomInputStream din = null;
                /*try
                 {
                 if (path.endsWith(".gz"))
                 din = new DicomInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(new File(path)), 256)));
                 else
                 din = new DicomInputStream(new File(path));
                   
                    
                 URI uri = new URI(path);
                 //System.out.println("Trying to find Plugin for: "+uri.toString());
                 StorageInterface plug = PluginController.getInstance().getStorageForSchema(uri);
                    
                 if(plug != null){
                 //System.out.println("Found Plugin For: "+uri.toString());
                        
                 Iterable<StorageInputStream> stream = plug.at(uri);
                 for(StorageInputStream str : stream)
                            
                 try {
                 din = new DicomInputStream(str.getInputStream());
                 } catch (IOException ex) {
                 Logger.getLogger(SearchDicomResult.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 }
                    
                 //DebugManager.getInstance().debug("Imagem: "+path+"..."+next);
                 } catch (URISyntaxException ex) {
                 Logger.getLogger(SearchDicomResult.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 */

                /**
                 * This code is refactored in a experimental branch Building a
                 * BasicDicomObject based on Indexing It will increase the
                 * performace
                 */
                BasicDicomObject result = new BasicDicomObject();
                if (queryLevel == QUERYLEVEL.PATIENT) {

                    // Experimental branch
                }
                // Fill fields of study now

                //System.out.println("Serie : "+ serieTmp);
                result.putString(Tag.InstitutionName, VR.CS, (String) sR.get("InstitutionName"));

                result.putString(Tag.StudyInstanceUID, VR.UI, (String) sR.get("InstitutionName"));
                result.putString(Tag.SeriesInstanceUID, VR.UI, (String) sR.get("SeriesInstanceUID"));
                result.putString(Tag.SOPInstanceUID, VR.UI, (String) sR.get("SOPInstanceUID"));
                result.putString(Tag.SeriesDescription, VR.LO, (String) sR.get("SeriesDescription"));
                result.putString(Tag.SeriesDate, VR.TM, (String) sR.get("SeriesDate"));
                result.putString(Tag.SeriesTime, VR.TM, (String) sR.get("SeriesTime"));
                result.putString(Tag.QueryRetrieveLevel, VR.LO, "IMAGE");

                result.putString(Tag.Modality, VR.CS, (String) sR.get("Modality"));

                result.putString(Tag.SeriesNumber, VR.IS, "" + (String) sR.get("SeriesNumber"));

                return result;

            } else if (queryLevel == QUERYLEVEL.STUDY || queryLevel == QUERYLEVEL.PATIENT) {

                Study studyTmp = (Study) next;
                BasicDicomObject result = new BasicDicomObject();
                String patientName = studyTmp.getParent().getPatientName();

                try {
                    patientName = new String(studyTmp.getParent().getPatientName().getBytes("ISO-8859-1"), "ISO-8859-1");
                } catch (Exception ex) {
                    Logger.getLogger(SearchDicomResult.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    result.putBytes(Tag.PatientName, VR.PN, patientName.getBytes("ISO-8859-1"));
                } catch (Exception ex) {
                    Logger.getLogger(SearchDicomResult.class.getName()).log(Level.SEVERE, null, ex);
                }

                //System.out.println("PatientName:"+patientName);
                result.putString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
                result.putString(Tag.PatientSex, VR.LO, studyTmp.getParent().getPatientSex());
                result.putString(Tag.PatientID, VR.LO, studyTmp.getParent().getPatientID());
                result.putString(Tag.PatientBirthDate, VR.DA, studyTmp.getParent().getPatientBirthDate());
                result.putString(Tag.StudyDate, VR.DA, studyTmp.getStudyData());
                result.putString(Tag.StudyID, VR.SH, studyTmp.getStudyID());
                result.putString(Tag.StudyTime, VR.TM, studyTmp.getStudyTime());
                result.putString(Tag.AccessionNumber, VR.SH, studyTmp.getAccessionNumber());
                result.putString(Tag.StudyInstanceUID, VR.UI, studyTmp.getStudyInstanceUID());
                result.putString(Tag.StudyDescription, VR.LO, studyTmp.getStudyDescription());
                String modality = studyTmp.getSeries().get(0).getModality(); // Point of Failure, fix me
                result.putString(Tag.ModalitiesInStudy, VR.CS, modality);
                result.putString(Tag.Modality, VR.CS, modality);
                result.putString(Tag.InstitutionName, VR.CS, studyTmp.getInstitutuionName());

                return result;

            } else if (queryLevel == QUERYLEVEL.SERIE) {
                // Serie

                Serie serieTmp = (Serie) next;
                BasicDicomObject result = new BasicDicomObject();
                //System.out.println("Serie : "+ serieTmp);
                result.putString(Tag.InstitutionName, VR.CS, serieTmp.getParent().getInstitutuionName());

                result.putString(Tag.StudyInstanceUID, VR.UI, serieTmp.getParent().getStudyInstanceUID());
                result.putString(Tag.SeriesInstanceUID, VR.UI, serieTmp.getSerieInstanceUID());
                result.putString(Tag.SeriesDescription, VR.LO, serieTmp.getSeriesDescription());
                result.putString(Tag.SeriesDate, VR.TM, "");
                result.putString(Tag.SeriesTime, VR.TM, "");
                result.putString(Tag.QueryRetrieveLevel, VR.LO, "SERIES");
                String modality = serieTmp.getModality(); // Point of Failure, fix me
                result.putString(Tag.Modality, VR.CS, modality);

                result.putString(Tag.SeriesNumber, VR.IS, "" + serieTmp.getSerieNumber());
                return result;

            } else {
                System.err.println("ERROR: WRONG QUERY LEVEL!");
            }

        }
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported. Nobody use it.");
    }
}
