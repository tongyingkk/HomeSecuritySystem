package view;

import com.github.lgooddatepicker.components.TimePicker;
import controller.SecurityService;
import model.Section;
import model.Section.SensorState;
import model.Sensor.SensorType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

class ActivationConfigurePanel extends JPanel {
    private static String[] SENSOR_ACTIVATION_STATES = new String[]
            { SensorState.ACTIVATED.name(), SensorState.DEACTIVATED.name(), SensorState.SCHEDULED.name()};

    private final List<Section> sectionList;
    private final List<JComboBox> sensorActivationStateComboboxList = new ArrayList<>();
    private final List<TimePicker> fromTimePickerList = new ArrayList<>();
    private final List<TimePicker> toTimePickerList = new ArrayList<>();

    ActivationConfigurePanel(SensorType sensorType) {
        setLayout(new GridLayout(0,1));
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING);
        SecurityService securityService = SecurityService.getInstance();
        sectionList = securityService.getSectionsWithSensorInstalled(sensorType);
        for (Section section : sectionList) {
            JPanel rowPanel = new JPanel(flowLayout);

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

            rowPanel.add(new JLabel(section.getId()));
            rowPanel.add(new JLabel(section.getName()));

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

            rowPanel.add(sensorActivationStateCombobox);
            rowPanel.add(timeSchedulingRow);

            rowPanel.setAlignmentX(LEFT_ALIGNMENT);
            add(rowPanel);
        }

        setEditable(false);
        JButton saveButton = new JButton("Edit");
        saveButton.addActionListener(e -> {
            if (saveButton.getText().equals("Edit")) {
                saveButton.setText("Save");
                setEditable(true);
            } else {
                saveButton.setText("Edit");
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
            }
        });
        add(saveButton);
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
