/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MonitorWindow.java
 *
 * Created on Jan 23, 2016, 5:14:03 PM
 */

package joymonitor;

import java.awt.Color;
import java.awt.Font;
import org.bbi.linuxjoy.*;

/**
 *
 * @author wira
 */
public class MonitorWindow extends javax.swing.JFrame {

    private LinuxJoystick j;
    private boolean noJoysticks = true;
    private MonitorCanvas c;

    /** Creates new form MonitorWindow */
    public MonitorWindow() {
        initComponents();
        enumerate();
        c = new MonitorCanvas(this);
        panelCanvasContainer.add(c);
        c.setSize(panelCanvasContainer.getSize());
        lblNativeStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        try {
            String version = NoJoy.getVersionString();
            lblNativeStatus.setText(version);
            txtNativeProperty.setEnabled(true);
            txtNativeProperty.setEditable(true);
            btnSetNativeProperty.setEnabled(true);
        } catch(UnsatisfiedLinkError e){
            lblNativeStatus.setText("Can not open native library");
            lblNativeStatus.setForeground(Color.RED);
        }
    }

    public final void enumerate() {
        cmbDevices.removeAllItems();

        if(j != null) {
            j.setCallback(null);
            j.stopPollingThread();
            j.close();
            j = null;
        }

        int[] joyInfo = JoyFactory.enumerate();
        if(joyInfo == null) {
            noJoysticks = true;
            cmbDevices.addItem("No devices found");
        } else {
            noJoysticks = false;
            for(int i = 0; i < joyInfo.length; i++) {
				if(joyInfo[i] != -1) {
					cmbDevices.addItem(String.format("%d: " + decodeJoyName(joyInfo[i]) +
							" (%d axes and %d buttons)", i,
							JoyFactory.AXES(joyInfo[i]),
							JoyFactory.BUTTONS(joyInfo[i])));
				} else {
					cmbDevices.addItem(i + ": Unknown");
				}
            }
        }
    }

    public String decodeJoyName(int joyInfo) {
        switch(joyInfo & 0xff) {
            case 0x00:
                return "Generic";
            case 0x01:
                return "Xbox360 Controller";
        }
        return "Unknown";
    }

    public boolean isJoystickOpen() {
        return j != null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        btnOpen = new javax.swing.JButton();
        cmbDevices = new javax.swing.JComboBox();
        btnExit = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelCanvasContainer = new javax.swing.JPanel();
        panelNativeTools = new javax.swing.JPanel();
        lblNativeStatus = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtNativeProperty = new javax.swing.JTextField();
        btnSetNativeProperty = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        tglStreamMonitor = new javax.swing.JToggleButton();
        btnClearStreamOutput = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtStreamOutput = new javax.swing.JTextArea();
        cmbStreamFilter = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JoyMonitor");

        jLabel1.setText("Controller :");

        btnOpen.setText("Open");
        btnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenActionPerformed(evt);
            }
        });

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        btnClose.setText("Close Joystick");
        btnClose.setEnabled(false);
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        javax.swing.GroupLayout panelCanvasContainerLayout = new javax.swing.GroupLayout(panelCanvasContainer);
        panelCanvasContainer.setLayout(panelCanvasContainerLayout);
        panelCanvasContainerLayout.setHorizontalGroup(
            panelCanvasContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 628, Short.MAX_VALUE)
        );
        panelCanvasContainerLayout.setVerticalGroup(
            panelCanvasContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 439, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Visualize", panelCanvasContainer);

        lblNativeStatus.setText("Status Message");

        jLabel2.setText("Set Native Property (format: index, key, value)");

        txtNativeProperty.setEditable(false);

        btnSetNativeProperty.setText("Set");
        btnSetNativeProperty.setEnabled(false);
        btnSetNativeProperty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetNativePropertyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelNativeToolsLayout = new javax.swing.GroupLayout(panelNativeTools);
        panelNativeTools.setLayout(panelNativeToolsLayout);
        panelNativeToolsLayout.setHorizontalGroup(
            panelNativeToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNativeToolsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelNativeToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNativeStatus)
                    .addComponent(jLabel2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelNativeToolsLayout.createSequentialGroup()
                        .addComponent(txtNativeProperty, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSetNativeProperty)))
                .addContainerGap())
        );
        panelNativeToolsLayout.setVerticalGroup(
            panelNativeToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNativeToolsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblNativeStatus)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelNativeToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNativeProperty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSetNativeProperty))
                .addContainerGap(342, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Native Tools", panelNativeTools);

        tglStreamMonitor.setText("Enable Event Stream Monitor");

        btnClearStreamOutput.setText("Clear Output");
        btnClearStreamOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearStreamOutputActionPerformed(evt);
            }
        });

        txtStreamOutput.setBackground(new java.awt.Color(-16777216,true));
        txtStreamOutput.setColumns(20);
        txtStreamOutput.setEditable(false);
        txtStreamOutput.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        txtStreamOutput.setForeground(new java.awt.Color(-16711936,true));
        txtStreamOutput.setRows(5);
        jScrollPane1.setViewportView(txtStreamOutput);

        cmbStreamFilter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Monitor All", "Monitor Buttons", "Monitor Axes" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(tglStreamMonitor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClearStreamOutput)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbStreamFilter, 0, 228, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tglStreamMonitor)
                    .addComponent(btnClearStreamOutput)
                    .addComponent(cmbStreamFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Event Stream", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbDevices, 0, 463, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOpen))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnClose)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 443, Short.MAX_VALUE)
                        .addComponent(btnExit)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnOpen)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(cmbDevices, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExit)
                    .addComponent(btnClose))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed
        if(!noJoysticks) {
            if(j != null) {
                j.setCallback(null);
                j.stopPollingThread();
                j.close();
            }
            j = JoyFactory.get(cmbDevices.getSelectedIndex());
            j.reset();
            c.setTotalAxes(j.getNumAxes());
            c.setTotalButtons(j.getNumButtons());
            j.setCallback(new JoyEventCallback(this, c));
            j.startPollingThread(5);            
            btnClose.setEnabled(true);
			c.repaint();
        }
    }//GEN-LAST:event_btnOpenActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        if(j != null) {
            j.setCallback(null);
            j.stopPollingThread();
            j.close();
        }
        System.exit(0);
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        if(j != null) {
            j.setCallback(null);
            j.stopPollingThread();
            j.close();
            j = null;
            c.repaint();
        }

        btnClose.setEnabled(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnSetNativePropertyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetNativePropertyActionPerformed
        String tokens[] = txtNativeProperty.getText().split(",");
        if(tokens.length == 3) {
            try {
                int index = Integer.parseInt(tokens[0].trim());
                int key = Integer.parseInt(tokens[1].trim());
                int value = Integer.parseInt(tokens[2].trim());
                NoJoy.setNativeProperty(index, key, value);
            } catch(Exception e) {
                System.err.println("Failed to parse: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnSetNativePropertyActionPerformed

    private void btnClearStreamOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearStreamOutputActionPerformed
        txtStreamOutput.setText("");
    }//GEN-LAST:event_btnClearStreamOutputActionPerformed

    public void appendStreamOutput(LinuxJoystickEvent ev) {
        if(tglStreamMonitor.isSelected()) {
            if(cmbStreamFilter.getSelectedIndex() == 0 ||
                    (cmbStreamFilter.getSelectedIndex() == 1 && ev.getType() == LinuxJoystickEvent.BUTTON) ||
                    (cmbStreamFilter.getSelectedIndex() == 2 && ev.getType() == LinuxJoystickEvent.AXIS)
                )
                txtStreamOutput.setText(txtStreamOutput.getText() + "\n" + ev);
                txtStreamOutput.setCaretPosition(txtStreamOutput.getText().length()-1);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearStreamOutput;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnSetNativeProperty;
    private javax.swing.JComboBox cmbDevices;
    private javax.swing.JComboBox cmbStreamFilter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblNativeStatus;
    private javax.swing.JPanel panelCanvasContainer;
    private javax.swing.JPanel panelNativeTools;
    private javax.swing.JToggleButton tglStreamMonitor;
    private javax.swing.JTextField txtNativeProperty;
    private javax.swing.JTextArea txtStreamOutput;
    // End of variables declaration//GEN-END:variables

}

