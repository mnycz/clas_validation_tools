/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author fizikci0147
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import java.util.HashMap;

import com.sun.prism.Graphics;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;

import org.jlab.clas.physics.Particle;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;

public class C12validtools extends DetectorResponse {
    static final boolean debug=false;
    int nEvents = 0;
    DataBank mcBank=null,ctrkBank=null,calBank=null,ctofBank=null;
    DataBank trkBank=null,tofBank=null,htccBank=null,ltccBank=null;
    DataBank recPartBank=null,recFtPartBank=null,recTrkBank=null,recFtBank=null;
    DataBank recCalBank=null,recSciBank=null,recCheBank=null;
    DataBank ftcBank=null,fthBank=null,ftpartBank=null,recBank=null,runBank=null;

    Map <Integer,List<Integer>> recCalMap=new HashMap<Integer,List<Integer>>();
    Map <Integer,List<Integer>> recCheMap=new HashMap<Integer,List<Integer>>();
    Map <Integer,List<Integer>> recSciMap=new HashMap<Integer,List<Integer>>();
    Map <Integer,List<Integer>> recTrkMap=new HashMap<Integer,List<Integer>>();

    IndexedList<DataGroup> dataGroups      = new IndexedList<DataGroup>(1);
    EmbeddedCanvasTabbed   canvasTabbed    = null;
    ArrayList<String>      canvasTabNames  = new ArrayList<String>();
    ArrayList<Double>      REC_Data        = new ArrayList<Double>();
    ArrayList<Particle> REC_DataArray      = new ArrayList<>();
    List<DetectorResponse> Scint_List      = new ArrayList<>();


    public static void main(String[] args){
        C12validtools c12vt = new C12validtools();

        c12vt.setAnalysisTabNames("REC Particle","Scintillator","Calorimeter","Cherenkov");

        c12vt.CreateHistos();

        //String fileName="/Users/fizikci0147/work/clas_work/clas_validation_tools/small.hipo";
        String fileName="/Users/michaelnycz/JLAB_Programs/clas_validation_tools/small.hipo";
        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            System.err.println("Cannot find input file.");
        }

        HipoDataSource reader = new HipoDataSource();
        reader.open(fileName);

        while (reader.hasEvent()) {
            DataEvent event = reader.getNextEvent();
            c12vt.getBanks(event);
            c12vt.ProcessEvent(event);
        }
        reader.close();
        //create a jframe
        JFrame frame = new JFrame("C12Validtools");
        frame.setSize(1200, 800);
        frame.add(c12vt.canvasTabbed);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //plot histograms
        c12vt.plotHistos();

    }

    private void plotHistos() {
        /* plotting ....*/
        //REC tab
        canvasTabbed.getCanvas("REC Particle").divide(2,2);
        canvasTabbed.getCanvas("REC Particle").setGridX(false);
        canvasTabbed.getCanvas("REC Particle").setGridY(false);
        canvasTabbed.getCanvas("REC Particle").cd(0);
        canvasTabbed.getCanvas("REC Particle").draw(dataGroups.getItem(1).getH1F("hi_p_pos"));
        canvasTabbed.getCanvas("REC Particle").cd(1);
        canvasTabbed.getCanvas("REC Particle").draw(dataGroups.getItem(1).getH1F("h_px"));
        canvasTabbed.getCanvas("REC Particle").cd(2);
        canvasTabbed.getCanvas("REC Particle").draw(dataGroups.getItem(1).getH1F("hvert_t"));
        //Scinttilator tab
        canvasTabbed.getCanvas("Scintillator").divide(2,2);
        canvasTabbed.getCanvas("Scintillator").setGridX(false);
        canvasTabbed.getCanvas("Scintillator").setGridY(false);
        canvasTabbed.getCanvas("Scintillator").cd(0);
        canvasTabbed.getCanvas("Scintillator").draw(dataGroups.getItem(2).getH1F("hsc_energy"));
        //___________________________________
        //Cherenkov tab
        canvasTabbed.getCanvas("Cherenkov").divide(2,2);
        canvasTabbed.getCanvas("Cherenkov").setGridX(false);
        canvasTabbed.getCanvas("Cherenkov").setGridY(false);
        canvasTabbed.getCanvas("Cherenkov").cd(0);
        canvasTabbed.getCanvas("Cherenkov").draw(dataGroups.getItem(3).getH1F("Num_phe"));
        F1D tmp = new F1D("tmp");
        tmp= Cherenkov_Fit(dataGroups.getItem(3).getH1F("num_phe"));
        canvasTabbed.getCanvas("Cherenkov").draw(tmp,"same");

        //_____________________________________

    }
    /*creating Histos*/
    private void CreateHistos() {
        //REC Particle histos
        H1F hi_p_pos = new H1F("hi_p_pos", "hi_p_pos", 100, 0.0, 8.0);
        hi_p_pos.setTitleX("p (GeV)");
        hi_p_pos.setTitleY("Counts");
        H1F hvert_x = new H1F("h_px", "h_px", 100, -0.1, 0.1);
        hvert_x.setTitleX("Px");
        hvert_x.setTitleY("Counts");
        H1F hvert_t = new H1F("hvert_t","hvert_t",500,120,140);
        hvert_t.setTitleX("Vt");
        hvert_t.setTitleY("Counts");
        H1F beta = new H1F("Beta","Beta",100,-1,3);
        beta.setTitleX("Beta");
        beta.setTitleY("Counts");
        DataGroup dg_pos = new DataGroup(1,1);
        dg_pos.addDataSet(hi_p_pos, 1);
        dg_pos.addDataSet(hvert_x, 2);
        dg_pos.addDataSet(hvert_t,3);
        dg_pos.addDataSet(beta,4);
        dataGroups.add(dg_pos, 1);
        //Scintillator histos
        H1F hsc_energy = new H1F("hsc_energy", "hsc_energy", 100, 0.0, 8.0);
        hsc_energy.setTitleX("Energy");
        hsc_energy.setTitleY("Counts");
        DataGroup dscinth = new DataGroup(1,1);
        dscinth.addDataSet(hsc_energy,1);
        dataGroups.add(dscinth, 2);
        //__Cherenkov_____
        H1F Nphe = new H1F("Num_phe","Num_phe",10,0,50);
        Nphe.setTitleX("# Photoelectrons");
        DataGroup dg_Cherenkov = new DataGroup(1,1);
        dg_Cherenkov.addDataSet(Nphe,1);
        dataGroups.add(dg_Cherenkov,3);
    }
    public void setAnalysisTabNames(String... names) {
        for(String name : names) {
            canvasTabNames.add(name);
        }
        canvasTabbed = new EmbeddedCanvasTabbed(names);
    }

    private DataBank getBank(DataEvent de,String bankName) {
        DataBank bank=null;
        if (de.hasBank(bankName)) {
            bank=de.getBank(bankName);
        }
        return bank;
    }

    private void getBanks(DataEvent de) {
        ctrkBank    = getBank(de,"CVTRec::Tracks");
        tofBank     = getBank(de,"FTOF::clusters");
        trkBank     = getBank(de,"TimeBasedTrkg::TBTracks");
        recPartBank = getBank(de,"REC::Particle");
        recFtPartBank = getBank(de,"RECFT::Particle");
        mcBank      = getBank(de,"MC::Particle");
        recCheBank  = getBank(de,"REC::Cherenkov");
        recCalBank  = getBank(de,"REC::Calorimeter");
        recSciBank  = getBank(de,"REC::Scintillator");
        ltccBank    = getBank(de,"LTCC::clusters");
        htccBank    = getBank(de,"HTCC::rec");
        recTrkBank  = getBank(de,"REC::Track");
        recFtBank   = getBank(de,"REC::ForwardTagger");
        ftcBank     = getBank(de,"FTCAL::clusters");
        fthBank     = getBank(de,"FTHODO::clusters");
        ftpartBank  = getBank(de,"FT::particles");
        calBank     = getBank(de,"ECAL::clusters");
        ctofBank    = getBank(de,"CTOF::hits");
        recBank     = getBank(de,"REC::Event");
        runBank     = getBank(de,"RUN::config");
        loadMaps();
    }

    public void loadMap(Map<Integer,List<Integer>> map, 
            DataBank fromBank, 
            DataBank toBank, 
            String idxVarName) {
        map.clear();
        if (fromBank==null) return;
        if (toBank==null) return;
        for (int ii=0; ii<fromBank.rows(); ii++) {
            final int iTo=fromBank.getInt(idxVarName,ii);
            if (map.containsKey(iTo)) {
                map.get(iTo).add(ii);
            }
            else {
                List<Integer> iFrom=new ArrayList<Integer>();
                map.put(iTo,iFrom);
                map.get(iTo).add(ii);
            }
        }
    }

    /**
     *
     * Load mapping from REC::Particle to REC::"Detector".
     *
     */
    public void loadMaps() {
        loadMap(recCalMap,recCalBank,recPartBank,"pindex");
        loadMap(recCheMap,recCheBank,recPartBank,"pindex");
        loadMap(recSciMap,recSciBank,recPartBank,"pindex");
        loadMap(recTrkMap,recTrkBank,recPartBank,"pindex");
    }


    public DataBank getDetectorBank(int detId) {
        DataBank bankTo=null;
        return null;
    }

    public void Response (int sector, int layer, int component){
        this.getDescriptor().setSectorLayerComponent(sector, layer, component);
    }

    //public void CherenkovResponse()

    private void ProcessEvent(DataEvent event) {
        nEvents++;
        int index = 0;
        float energy = 0;
        float time=0;
        float x,y,z=0;
        int sector;
        int layer;
        int paddle=0;
        float nphe= 0;
        if (recBank==null || recPartBank==null || recFtBank==null || recFtPartBank==null) return;
        if (debug) {
            System.out.println("\n\n#############################################################\n");
            if (ftpartBank!=null) ftpartBank.show();
            recFtBank.show();
            recPartBank.show();
        }

        if ((nEvents % 10000) == 0) System.out.println("Analyzed " + nEvents + " events");
       if(event.hasBank("REC::Particle")==true) {

            int rows = recPartBank.rows();
            for (int loop = 0; loop < rows; loop++) {
                int pidCode = 0;
                if (recPartBank.getByte("charge", loop) == -1) pidCode = 11;
                else if (recPartBank.getByte("charge", loop) == 1) pidCode = 211;
                else pidCode = 22;

                Particle recParticle = new Particle(
                        pidCode,
                        recPartBank.getFloat("px", loop),
                        recPartBank.getFloat("py", loop),
                        recPartBank.getFloat("pz", loop),
                        recPartBank.getFloat("vx", loop),
                        recPartBank.getFloat("vy", loop),
                        recPartBank.getFloat("vz", loop));
                float vert_t = recPartBank.getFloat("vt", loop);
                REC_DataArray.add(recParticle);
                //System.out.println(recParticle.charge());
                dataGroups.getItem(1).getH1F("hi_p_pos").fill(recParticle.p());
                dataGroups.getItem(1).getH1F("h_px").fill(recParticle.px());
                dataGroups.getItem(1).getH1F("hvert_t").fill(vert_t);


            }
        }

        if (event.hasBank("REC::Scintillator")){
            C12validtools Response = new C12validtools();
            int rows = recSciBank.rows();
            for(int loop=0;loop<rows;loop++) {
                index  = recSciBank.getInt("pindex",loop);
                layer  = recSciBank.getByte("layer",loop);
                sector = recSciBank.getByte("sector",loop);
                paddle = recSciBank.getInt("component",loop);
                energy = recSciBank.getFloat("energy",loop);
                time   = recSciBank.getFloat("time",loop);
                x = recSciBank.getFloat("x",loop);
                y = recSciBank.getFloat("y",loop);
                z = recSciBank.getFloat("z",loop);
                dataGroups.getItem(2).getH1F("hsc_energy").fill(energy);
                Response.setPosition(layer,sector,paddle);
                Response.setEnergy((energy));
                Response.setEnergy((time));
            }
            Scint_List.add(Response);
            //Energy.put(nEvents,energy);
        }


        if (event.hasBank("REC::Cherenkov")){
            int rows = recCheBank.rows();
            for(int loop=0;loop<rows;loop++){
                index= recCheBank.getInt("pindex",loop);
                double x_cer = recCheBank.getFloat("x",loop);
                double y_cer = recCheBank.getFloat("y",loop);
                double z_cer = recCheBank.getFloat("z",loop);
                double time_cer = recCheBank.getFloat("time",loop);
                nphe = recCheBank.getFloat("nphe",loop);
                dataGroups.getItem(3).getH1F("Num_phe").fill(nphe);
            }

        }

    }


    public F1D Cherenkov_Fit(H1F histo){
        F1D f1 = new F1D("f1","[amp]*gaus(x,[mean],[sigma])", 0, 50.0);
        DataFitter.fit(f1, histo, "Q");
        f1.setParameter(0, 50.0);
        f1.setParameter(1, 30.0);
        f1.setParameter(2, 5.0);
        f1.setLineColor(5);
        f1.setLineWidth(7);
        f1.setOptStat(111110);
        return f1;
    }

    public void Read_RECArray(ArrayList<Particle> Data) {
;
        /*System.out.println(Data.get(0));
        System.out.println(Data.get(1));
        System.out.println(Data.size());
         */
    }

}