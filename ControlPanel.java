import java.applet.*;
import java.awt.*;
import javax.swing.JLabel;

public class ControlPanel extends Frame {
    Kernel kernel;
    Button runButton = new Button("run");
    Button stepButton = new Button("step");
    Button resetButton = new Button("reset");
    Button exitButton = new Button("exit");

    // Arrays para botones y etiquetas
    Button[] pageButtons = new Button[64];
    Label[] physicalLabels = new Label[64];

    // Info Labels
    Label statusValueLabel = new Label("STOP", Label.LEFT);
    Label timeValueLabel = new Label("0", Label.LEFT);
    Label instructionValueLabel = new Label("NONE", Label.LEFT);
    Label addressValueLabel = new Label("NULL", Label.LEFT);
    Label pageFaultValueLabel = new Label("NO", Label.LEFT);
    Label virtualPageValueLabel = new Label("x", Label.LEFT);
    Label physicalPageValueLabel = new Label("0", Label.LEFT);
    Label RValueLabel = new Label("0", Label.LEFT);
    Label MValueLabel = new Label("0", Label.LEFT);
    Label inMemTimeValueLabel = new Label("0", Label.LEFT);
    Label lastTouchTimeValueLabel = new Label("0", Label.LEFT);
    Label lowValueLabel = new Label("0", Label.LEFT);
    Label highValueLabel = new Label("0", Label.LEFT);
    Label segmentLabel = new Label("", Label.LEFT);
    Label pagesLabel = new Label("", Label.LEFT);
    
    // NUEVO: Label para la fragmentacion externa
    public JLabel fragmentationValueLabel = new JLabel("Frag. externa: 0");

    public ControlPanel() {
        super();
    }

    public ControlPanel(String title) {
        super(title);
    }

    public void init(Kernel useKernel, String commands, String config) {
        kernel = useKernel;
        kernel.setControlPanel(this);
        setLayout(null);
        setBackground(Color.white);
        setForeground(Color.black);
        resize(635, 545);
        setFont(new Font("Courier", 0, 12));

        // Botones de control
        runButton.setForeground(Color.blue);
        runButton.setBackground(Color.lightGray);
        runButton.reshape(0, 25, 70, 15);
        add(runButton);

        stepButton.setForeground(Color.blue);
        stepButton.setBackground(Color.lightGray);
        stepButton.reshape(70, 25, 70, 15);
        add(stepButton);

        resetButton.setForeground(Color.blue);
        resetButton.setBackground(Color.lightGray);
        resetButton.reshape(140, 25, 70, 15);
        add(resetButton);

        exitButton.setForeground(Color.blue);
        exitButton.setBackground(Color.lightGray);
        exitButton.reshape(210, 25, 70, 15);
        add(exitButton);

        // Inicializar los arrays de botones y etiquetas
        for (int i = 0; i < 64; i++) {
            pageButtons[i] = new Button("page " + i);
            pageButtons[i].setForeground(Color.magenta);
            pageButtons[i].setBackground(Color.lightGray);
            pageButtons[i].reshape((i < 32 ? 0 : 140), ((i % 32) + 2) * 15 + 25, 70, 15);
            add(pageButtons[i]);

            physicalLabels[i] = new Label(null, Label.CENTER);
            physicalLabels[i].setForeground(Color.red);
            physicalLabels[i].setFont(new Font("Courier", 0, 10));
            physicalLabels[i].reshape((i < 32 ? 70 : 210), ((i % 32) + 2) * 15 + 25, 60, 15);
            add(physicalLabels[i]);
        }

        // Colores de los segmentos para los primeros 32 botones
        Color[] segmentColors = {
            new Color(255, 200, 200), // S1
            new Color(200, 255, 200), // S2
            new Color(200, 200, 255), // S3
            new Color(255, 255, 200), // S4
            new Color(200, 255, 255), // S5
        };
        for (int i = 0; i < 32; i++) {
            int seg = 0;
            for (int s = 0; s < Kernel.SEGMENTS.length; s++) {
                for (int p = 0; p < Kernel.SEGMENTS[s].length; p++) {
                    if (Kernel.SEGMENTS[s][p] == i) {
                        seg = s;
                    }
                }
            }
            pageButtons[i].setBackground(segmentColors[seg]);
        }
        
        // Etiquetas de informacion
        Label statusLabel = new Label("status: ", Label.LEFT);
        statusLabel.reshape(285, 25, 65, 15);
        add(statusLabel);
        statusValueLabel.reshape(345, 25, 100, 15);
        add(statusValueLabel);

        Label timeLabel = new Label("time: ", Label.LEFT);
        timeLabel.reshape(285, 40, 50, 15);
        add(timeLabel);
        timeValueLabel.reshape(345, 40, 100, 15);
        add(timeValueLabel);

        Label instructionLabel = new Label("instruction: ", Label.LEFT);
        instructionLabel.reshape(285, 70, 100, 15);
        add(instructionLabel);
        instructionValueLabel.reshape(385, 70, 100, 15);
        add(instructionValueLabel);

        Label addressLabel = new Label("address: ", Label.LEFT);
        addressLabel.reshape(285, 85, 85, 15);
        add(addressLabel);
        addressValueLabel.reshape(385, 85, 230, 15);
        add(addressValueLabel);

        Label pageFaultLabel = new Label("page fault: ", Label.LEFT);
        pageFaultLabel.reshape(285, 115, 100, 15);
        add(pageFaultLabel);
        pageFaultValueLabel.reshape(385, 115, 100, 15);
        add(pageFaultValueLabel);

        Label virtualPageLabel = new Label("virtual page: ", Label.LEFT);
        virtualPageLabel.reshape(285, 145, 110, 15);
        add(virtualPageLabel);
        virtualPageValueLabel.reshape(395, 145, 200, 15);
        add(virtualPageValueLabel);

        Label physicalPageLabel = new Label("physical page: ", Label.LEFT);
        physicalPageLabel.reshape(285, 160, 110, 15);
        add(physicalPageLabel);
        physicalPageValueLabel.reshape(395, 160, 200, 15);
        add(physicalPageValueLabel);

        Label RLabel = new Label("R: ", Label.LEFT);
        RLabel.reshape(285, 175, 110, 15);
        add(RLabel);
        RValueLabel.reshape(395, 175, 200, 15);
        add(RValueLabel);

        Label MLabel = new Label("M: ", Label.LEFT);
        MLabel.reshape(285, 190, 110, 15);
        add(MLabel);
        MValueLabel.reshape(395, 190, 200, 15);
        add(MValueLabel);

        Label inMemTimeLabel = new Label("inMemTime: ", Label.LEFT);
        inMemTimeLabel.reshape(285, 205, 110, 15);
        add(inMemTimeLabel);
        inMemTimeValueLabel.reshape(395, 205, 200, 15);
        add(inMemTimeValueLabel);

        Label lastTouchTimeLabel = new Label("lastTouchTime: ", Label.LEFT);
        lastTouchTimeLabel.reshape(285, 220, 110, 15);
        add(lastTouchTimeLabel);
        lastTouchTimeValueLabel.reshape(395, 220, 200, 15);
        add(lastTouchTimeValueLabel);

        Label lowLabel = new Label("low: ", Label.LEFT);
        lowLabel.reshape(285, 235, 110, 15);
        add(lowLabel);
        lowValueLabel.reshape(395, 235, 230, 15);
        add(lowValueLabel);

        Label highLabel = new Label("high: ", Label.LEFT);
        highLabel.reshape(285, 250, 110, 15);
        add(highLabel);
        highValueLabel.reshape(395, 250, 230, 15);
        add(highValueLabel);

        segmentLabel.reshape(285, 265, 150, 15);
        segmentLabel.setFont(new Font("Courier", Font.BOLD, 12));
        add(segmentLabel);

        pagesLabel.reshape(285, 280, 300, 15);
        pagesLabel.setFont(new Font("Courier", Font.PLAIN, 12));
        add(pagesLabel);

        // NUEVO: Agregando la etiqueta de fragmentacion externa
        fragmentationValueLabel.reshape(285, 295, 300, 15);
        fragmentationValueLabel.setFont(new Font("Courier", Font.PLAIN, 12));
        add(fragmentationValueLabel);

        kernel.init(commands, config);

        show();
    }

    // Metodos para actualizar las etiquetas de la GUI
    public void setPagesInvolved(String text) {
        if (text == null || text.isEmpty()) {
            pagesLabel.setText("");
        } else {
            pagesLabel.setText("Paginas: " + text);
        }
    }

    public void paintPage(Page page) {
        virtualPageValueLabel.setText(Integer.toString(page.id));
        physicalPageValueLabel.setText(Integer.toString(page.physical));
        RValueLabel.setText(Integer.toString(page.R));
        MValueLabel.setText(Integer.toString(page.M));
        inMemTimeValueLabel.setText(Integer.toString(page.inMemTime));
        lastTouchTimeValueLabel.setText(Integer.toString(page.lastTouchTime));
        lowValueLabel.setText(Long.toString(page.low, Kernel.addressradix));
        highValueLabel.setText(Long.toString(page.high, Kernel.addressradix));

        int segnum = kernel.getSegmentForPage(page.id);
        if (segnum != -1) {
            segmentLabel.setText("Segmento: S" + (segnum + 1));
        } else {
            segmentLabel.setText("Segmento: N/A");
        }
    }

    public void setStatus(String status) {
        statusValueLabel.setText(status);
    }

    public void addPhysicalPage(int pageNum, int physicalPage) {
        if (physicalPage >= 0 && physicalPage < 64) {
            physicalLabels[physicalPage].setText("page " + pageNum);
        }
    }

    public void removePhysicalPage(int physicalPage) {
        if (physicalPage >= 0 && physicalPage < 64) {
            physicalLabels[physicalPage].setText(null);
        }
    }

    // Manejo de eventos de botones
    public boolean action(Event e, Object arg) {
        if (e.target == runButton) {
            setStatus("RUN");
            runButton.disable();
            stepButton.disable();
            resetButton.disable();
            kernel.run();
            setStatus("STOP");
            resetButton.enable();
            return true;
        } else if (e.target == stepButton) {
            setStatus("STEP");
            kernel.step();
            if (kernel.runcycles == kernel.runs) {
                stepButton.disable();
                runButton.disable();
            }
            setStatus("STOP");
            return true;
        } else if (e.target == resetButton) {
            kernel.reset();
            runButton.enable();
            stepButton.enable();
            return true;
        } else if (e.target == exitButton) {
            System.exit(0);
            return true;
        }
        
        // Manejo de eventos para los botones de pagina
        for (int i = 0; i < 64; i++) {
            if (e.target == pageButtons[i]) {
                kernel.getPage(i);
                return true;
            }
        }
        
        return false;
    }
}
