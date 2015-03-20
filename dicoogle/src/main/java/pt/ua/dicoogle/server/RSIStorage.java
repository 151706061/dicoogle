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
package pt.ua.dicoogle.server;

import pt.ua.dicoogle.core.ServerSettings;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;

///import org.dcm4che2.net.Executor;
/** dcm4che doesn't support Executor anymore, so now import from java.util */ 
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.Status;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.StorageService;
import org.dcm4che2.net.service.VerificationService;


import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.IndexerInterface;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.datastructs.Report;
import pt.ua.dicoogle.sdk.task.Task;


/**
 * DICOM Storage Service is provided by this class
 * @author Marco Pereira
 */

public class RSIStorage extends StorageService
{
    
    private SOPList list;
    private ServerSettings settings;
        
    private Executor executor = new NewThreadExecutor("RSIStorage");
    private Device device = new Device("RSIStorage");
    private NetworkApplicationEntity nae = new NetworkApplicationEntity();
    private NetworkConnection nc = new NetworkConnection();
    
    private String path;
    private DicomDirCreator dirc;
    
    private int fileBufferSize = 256;
    private int threadPoolSize = 10;
    
    private ExecutorService pool = Executors.newFixedThreadPool(threadPoolSize);
    
    private boolean gzip = ServerSettings.getInstance().isGzipStorage();;
   
    private BlockingQueue<URI> queue = new LinkedBlockingQueue<URI>();
    
    PluginController pluginController;
    /**
     * 
     * @param Services List of supported SOP Classes
     * @param l list of Supported SOPClasses with supported Transfer Syntax
     * @param s Server Settings for this execution of the storage service
     */
    
    public RSIStorage(PluginController pController, String [] Services, SOPList l)
    {
        //just because the call to super must be the first instruction
        super(Services); 
        
        pluginController = pController;
            //our configuration format
            list = l;
            settings = ServerSettings.getInstance();

            path = settings.getPath();
            if (path == null) {
                path = "/dev/null";
            }

            device.setNetworkApplicationEntity(nae);
            device.setNetworkConnection(nc);
            nae.setNetworkConnection(nc);

            //we accept assoociations, this is a server
            nae.setAssociationAcceptor(true);
            //we support the VerificationServiceSOP
            nae.register(new VerificationService());
            //and the StorageServiceSOP
            nae.register(this);

            nae.setAETitle(settings.getAE());

            nc.setPort(settings.getStoragePort());
            
            
            this.nae.setInstalled(true);
            this.nae.setAssociationAcceptor(true);
            this.nae.setAssociationInitiator(false);
            
            
            ServerSettings s  = ServerSettings.getInstance();
            this.nae.setDimseRspTimeout(60000*300);
            this.nae.setIdleTimeout(60000*300);
            this.nae.setMaxPDULengthReceive(s.getMaxPDULengthReceive()+1000);
            this.nae.setMaxPDULengthSend(s.getMaxPDULenghtSend()+1000);
            this.nae.setRetrieveRspTimeout(60000*300);
            
                    
            String[] array = settings.getCAET();
            if (array != null) {
                //nae.setPreferredCallingAETitle(settings.getCAET());
            }

            initTS(Services);       
    }
    /**
     *  Sets the tranfer capability for this execution of the storage service
     *  @param Services Services to be supported
     */
    private void initTS(String [] Services)
    {
        int count = list.getAccepted();
        //System.out.println(count);
        TransferCapability[] tc = new TransferCapability[count + 1];
        String [] Verification = {UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian};
        String [] TS;
        TransfersStorage local;        

        tc[0] = new TransferCapability(UID.VerificationSOPClass, Verification, TransferCapability.SCP);
        int j = 0;
        for (int i = 0; i < Services.length; i++)
        {
            count = 0;
            local = list.getTS(Services[i]);  
            if (local.getAccepted())
            {
                TS = local.getVerboseTS();
                if(TS != null)
                {                

                    tc[j+1] = new TransferCapability(Services[i], TS, TransferCapability.SCP);
                    j++;
                }                        
            }
        }
        
        nae.setTransferCapability(tc);
    }
      
    @Override
    /**
     * Called when a C-Store Request has been accepted
     * Parameters defined by dcm4che2
     */
    public void cstore(final Association as, final int pcid, DicomObject rq, PDVInputStream dataStream, String tsuid) throws DicomServiceException, IOException
    {
        //DebugManager.getInstance().debug(":: Verify Permited AETs @ C-Store Request ");

        boolean permited = false;

        if(ServerSettings.getInstance().getPermitAllAETitles()){
            permited = true;
        }
        else {
            String permitedAETs[] = ServerSettings.getInstance().getCAET();

            for (int i = 0; i < permitedAETs.length; i++) {
                if (permitedAETs[i].equals(as.getCallingAET())) {
                    permited = true;
                    break;
                }
            }
        }

        if (!permited) {
            //DebugManager.getInstance().debug("Client association NOT permited: " + as.getCallingAET() + "!");
            System.err.println("Client association NOT permited: " + as.getCallingAET() + "!");
            as.abort();
            
            return;
        } else {
            //DebugManager.getInstance().debug("Client association permited: " + as.getCallingAET() + "!");
            System.err.println("Client association permited: " + as.getCallingAET() + "!");
        }

        final DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
        onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
        as.writeDimseRSP(pcid, rsp);       
        //onCStoreRSP(as, pcid, rq, dataStream, tsuid, rsp);
    }
    
    @Override
    /**
     * Actually do the job of saving received file on disk
     * on this server with extras such as Lucene indexing
     * and DICOMDIR update
     */
    protected void onCStoreRQ(Association as, int pcid, DicomObject rq, PDVInputStream dataStream, String tsuid, DicomObject rsp) throws IOException, DicomServiceException 
    {  
        try
        {
            /*
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            
            DicomObject d = dataStream.readDataset();
            
            System.out.println(d.get(Tag.TransferSyntaxUID));
                       
            String extraPath= getDirectory(d);
            new File(extraPath).mkdirs(); 
            long time = System.currentTimeMillis();
            String fileStr = getFullPathCache(extraPath, d);
            if (gzip)
            {
                fileStr += ".gz";
            }
            
            //first we write the file to a temporary location
            BasicDicomObject fmi = new BasicDicomObject();
            fmi.initFileMetaInformation(cuid, iuid, tsuid);  
            
            File file = new File(fileStr);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos,fileBufferSize);
            DicomOutputStream dos = null;
            if (gzip)
            {
                dos = new DicomOutputStream(new GZIPOutputStream(bos));
            }
            else
            {
                dos = new DicomOutputStream(bos);
            }
            
            //dos.writeFileMetaInformation(fmi);  
            
            d.initFileMetaInformation(cuid, iuid, tsuid);  
            dos.writeDicomFile(d);
            //dataStream.copyTo(dos);
            
            
            dos.close();
            
            System.out.println(file.getAbsolutePath());

            
            //core.indexQueue(file.getAbsolutePath(), true);
            queue.add(file.getAbsolutePath());
            
            */
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            
            DicomObject d = dataStream.readDataset();
            
            d.initFileMetaInformation(cuid, iuid, tsuid);
            
            Iterable <StorageInterface> plugins = pluginController.getStoragePlugins(true);
            if(plugins == null){
                //System.out.println("There is no default plugin...");
            
                //System.out.println("Number of StoragePlugins: "+PluginController.getInstance().getStorageInterfaces().size());
                
                //System.out.println(PluginController.getInstance().getIndexingPlugins().size());
            }
            URI uri = null;
            for (StorageInterface storage : plugins)
            {
                uri = storage.store(d);
                if(uri != null)
                    queue.add(uri);
            }
            
            //System.out.println("Another successfull stored object xD");
            //System.out.println("URI: "+uri);
            
            //InputStream retrievedFile = plugin.retrieve(uri);
            //byte[] byteArr = ByteStreams.toByteArray(retrievedFile);
                       
        } catch (IOException e) {
           //System.out.println(e.toString());
           throw new DicomServiceException(rq, Status.ProcessingFailure, e.getMessage());          
         }
    }
    
    
    class Indexer extends Thread
    {
        public Collection<IndexerInterface> plugins;
        
        public void run()
        {
            while (true)
            {
                try 
                {
                    URI exam = queue.take();
                    
                    if(exam != null)
                    {
                        Task<Report> task = pluginController.indexAllClosure(exam);
                        task.run();
                        Report reports = task.get();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger("dicoogle").log(Level.SEVERE, null, ex);
                }
                 
            }
            
        }
    }
    
    
    private String getFullPath(DicomObject d)
    {
    
        return getDirectory(d) + File.separator + getBaseName(d);
    
    }
    
    
    private String getFullPathCache(String dir, DicomObject d)
    {    
        return dir + File.separator + getBaseName(d);
 
    }
    
    
    
    private String getBaseName(DicomObject d)
    {
        String result = "UNKNOWN.dcm";
        String sopInstanceUID = d.getString(Tag.SOPInstanceUID);
        return sopInstanceUID+".dcm";
    }
    
    
    private String getDirectory(DicomObject d)
    {
    
        String result = "UN";
        
        String institutionName = d.getString(Tag.InstitutionName);
        String modality = d.getString(Tag.Modality);
        String studyDate = d.getString(Tag.StudyDate);
        String accessionNumber = d.getString(Tag.AccessionNumber);
        String studyInstanceUID = d.getString(Tag.StudyInstanceUID);
        String patientName = d.getString(Tag.PatientName);
        
        if (institutionName==null || institutionName.equals(""))
        {
            institutionName = "UN_IN";
        }
        institutionName = institutionName.trim();
        institutionName = institutionName.replace(" ", "");
        institutionName = institutionName.replace(".", "");
        institutionName = institutionName.replace("&", "");

        
        if (modality == null || modality.equals(""))
        {
            modality = "UN_MODALITY";
        }
        
        if (studyDate == null || studyDate.equals(""))
        {
            studyDate = "UN_DATE";
        }
        else
        {
            try
            {
                String year = studyDate.substring(0, 4);
                String month =  studyDate.substring(4, 6);
                String day =  studyDate.substring(6, 8);
                
                studyDate = year + File.separator + month + File.separator + day;
                
            }
            catch(Exception e)
            {
                e.printStackTrace();
                studyDate = "UN_DATE";
            }
        }
        
        if (accessionNumber == null || accessionNumber.equals(""))
        {
            patientName = patientName.trim();
            patientName = patientName.replace(" ", "");
            patientName = patientName.replace(".", "");
            patientName = patientName.replace("&", "");
            
            if (patientName == null || patientName.equals(""))
            {
                if (studyInstanceUID == null || studyInstanceUID.equals(""))
                {
                    accessionNumber = "UN_ACC";
                }
                else
                {
                    accessionNumber = studyInstanceUID;
                }
            }
            else
            {
                accessionNumber = patientName;
                
            }
            
        }
        
        result = path+File.separator+institutionName+File.separator+modality+File.separator+studyDate+File.separator+accessionNumber;
        
        return result;
        
    }
    private Indexer indexer = new Indexer();
    /*
     * Start the Storage Service 
     * @throws java.io.IOException
     */
    public void start() throws IOException
    {       
        //dirc = new DicomDirCreator(path, "Dicoogle");
        pool = Executors.newFixedThreadPool(threadPoolSize);
        device.startListening(executor); 
        indexer.start();
        

    } 
    
    /**
     * Stop the storage service 
     */
    public void stop()
    {
        this.pool.shutdown();
        try {
            pool.awaitTermination(6, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            Logger.getLogger(RSIStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        device.stopListening();
        
        //dirc.dicomdir_close();
    }   
}
