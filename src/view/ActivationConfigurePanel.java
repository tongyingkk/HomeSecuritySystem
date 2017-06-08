package view;

import com.github.lgooddatepicker.components.TimePicker;
import controller.SecurityService;
import model.Section;
import model.Section.SensorState;
import model.Sensor.SensorType;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import net.miginfocom.layout.Grid;
import net.miginfocom.swing.MigLayout;
import javax.*;

class ActivationConfigurePanel extends JPanel {
    private static String[] SENSOR_ACTIVATION_STATES = new String[]
            { SensorState.ACTIVATED.name(), SensorState.DEACTIVATED.name(), SensorState.SCHEDULED.name()};

    private final List<Section> sectionList;
    private final List<JComboBox> sensorActivationStateComboboxList = new ArrayList<>();
    private final List<TimePicker> fromTimePickerList = new ArrayList<>();
    private final List<TimePicker> toTimePickerList = new ArrayList<>();
    JTextField passwordTextField;
    JLabel passwordLable;

    ActivationConfigurePanel(SensorType sensorType) {
//        setLayout(new MigLayout("","grow"));
//       FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);

      //  JPanel panetotal = new JPanel();


      setLayout(new GridLayout(3,2));
      FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
        SecurityService securityService = SecurityService.getInstance();
        sectionList = securityService.getSectionsWithSensorInstalled(sensorType);
        for (Section section : sectionList) {
           // JPanel a  = new JPanel();

            JPanel rowPanel = new JPanel();

          rowPanel.setLayout(new MigLayout("","Center","Center"));
          rowPanel.setBorder(new TitledBorder(section.getName()));
          rowPanel.setBorder(BorderFactory.createEtchedBorder(Color.blue,Color.yellow));
          rowPanel.setBackground(Color.WHITE);


            TimePicker fromTimePicker = new TimePicker();
            fromTimePicker.setTime(section.getSensorScheduledFromTime(sensorType));
            fromTimePickerList.add(fromTimePicker);
            TimePicker toTimePicker = new TimePicker();
            toTimePicker.setTime(section.getSensorScheduledToTime(sensorType));
            toTimePickerList.add(toTimePicker);

            JPanel timeSchedulingRow = new JPanel();
            timeSchedulingRow.add(new JLabel("From"));
            timeSchedulingRow.add(fromTimePicker);
            timeSchedulingRow.add(new JLabel("To"));
            timeSchedulingRow.add(toTimePicker);

            JComboBox<String> sensorActivationStateCombobox = new JComboBox<>(SENSOR_ACTIVATION_STATES);
            sensorActivationStateComboboxList.add(sensorActivationStateCombobox);

           rowPanel.add(new JLabel(section.getName()),"align right, growx,wrap");

            switch (section.getSensorState(sensorType)) {
                case ACTIVATED:
                    sensorActivationStateCombobox.setSelectedIndex(0);
                    timeSchedulingRow.setVisible(false);
                    break;
                case DEACTIVATED:
                    sensorActivationStateCombobox.setSelectedIndex(1);
                    timeSchedulingRow.setVisible(false);
                    break;
                case SCHEDULED:
                    sensorActivationStateCombobox.setSelectedIndex(2);
                    timeSchedulingRow.setVisible(true);
                    break;
                default:
                    break;
            }

            sensorActivationStateCombobox.addItemListener(event -> {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    SensorState newSensorState = SensorState.valueOf((String) event.getItem());
                    timeSchedulingRow.setVisible(newSensorState.equals(SensorState.SCHEDULED));
                }
            });

            rowPanel.add(sensorActivationStateCombobox,"wrap");
            rowPanel.add(timeSchedulingRow,"wrap");

          rowPanel.setAlignmentX(LEFT_ALIGNMENT);
          add(rowPanel);
        }

        JPanel panel = new JPanel();
        add(panel);
        setEditable(false);
        passwordLable = new JLabel("Password");
        passwordLable.setVisible(false);

        passwordTextField = new JTextField();
        passwordTextField.setPreferredSize(new Dimension(80,25));
        passwordTextField.setVisible(false);


        JPanel pane2 = new JPanel();
        pane2.add(passwordLable);
        pane2.add(passwordTextField);
        JButton actionButton = new JButton("Edit");
        Font bigFont = new Font("serif",Font.BOLD,16);
        actionButton.setFont(bigFont);

        actionButton.addActionListener(e -> {
            if (actionButton.getText().equals("Save")) {
                if(SecurityService.getInstance().getCustomer().passwordMatched(passwordTextField.getText())) {
                    actionButton.setText("Edit");
                    setEditable(false);


                    for (int i = 0; i < sectionList.size(); i++) {
                        Section section = sectionList.get(i);
                        SensorState newSensorState =
                                SensorState.valueOf((String) sensorActivationStateComboboxList.get(i).getSelectedItem());
                        section.setSensorState(sensorType, newSensorState);
                        if (newSensorState.equals(SensorState.SCHEDULED)) {
                            section.setSensorScheduledFromTime(sensorType, fromTimePickerList.get(i).getTime());
                            section.setSensorScheduledToTime(sensorType, toTimePickerList.get(i).getTime());
                        }
                    }

                    securityService.saveSensorConfig();

                    passwordLable.setVisible(false);
                    passwordTextField.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(this,"Password is not correct, try again");
                }

            } else {
                setEditable(true);
                actionButton.setText("Save");
                passwordLable.setVisible(true);
                passwordTextField.setVisible(true);
            }
        });

      pane2.add(actionButton, BorderLayout.SOUTH);
      add(pane2);

    }

    private void setEditable(boolean editable) {
        for (JComboBox sensorStateCombobox : sensorActivationStateComboboxList) {
            sensorStateCombobox.setEnabled(editable);
        }
        for (TimePicker timePicker : fromTimePickerList) {
            timePicker.setEnabled(editable);
        }
        for (TimePicker timePicker : toTimePickerList) {
            timePicker.setEnabled(editable);
        }
    }
}
