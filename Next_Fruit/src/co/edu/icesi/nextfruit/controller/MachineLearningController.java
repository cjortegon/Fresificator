package co.edu.icesi.nextfruit.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

import co.edu.icesi.nextfruit.modules.Model;
import co.edu.icesi.nextfruit.mvc.interfaces.Attachable;
import co.edu.icesi.nextfruit.mvc.interfaces.Initializable;
import co.edu.icesi.nextfruit.mvc.interfaces.Updateable;
import co.edu.icesi.nextfruit.views.MachineLearningWindow;


/**
 * 
 * @author JuanD
 *
 */
public class MachineLearningController implements Initializable, ActionListener{

	
	private static final String CHOOSE_IMAGES_DIR = "ChImgsDir";
	private static final String CHOOSE_TRAINING_SET_DIR = "chTrnSetDir";
	private static final String CHOOSE_CLASSIFIER_DIR = "chClssfrDir";
	
	private static final String GENERATE_TRAINING_SET = "SaveTrainingSet";
	private static final String LOAD_TRAINING_SET = "ColorChecker";
	private static final String TRAIN_CLASSIFIER = "TrainClassifier";
	
	private Model model;
	private MachineLearningWindow mLWindow;
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		System.out.println("actionPerformed: "+e.getActionCommand());
		
		switch (e.getActionCommand()) {
		case CHOOSE_IMAGES_DIR:
			break;
			
		case CHOOSE_TRAINING_SET_DIR:
			File file = loadFile("Load Training Set");
			
			if(file != null){
				
			}
			
			break;
			
		case CHOOSE_CLASSIFIER_DIR:
			break;
			
		case GENERATE_TRAINING_SET:
			break;
			
		case LOAD_TRAINING_SET:
			break;
			
		case TRAIN_CLASSIFIER:
			break;

		}
		
	}

	
	@Override
	public void init(Attachable model, Updateable view) {
		
		this.model = (Model) model;
		this.mLWindow = (MachineLearningWindow) view;
		addListeners();	
	}
	
	
	/**
	 * 
	 */
	private void addListeners(){
		
		mLWindow.getBtChooseImagesDir().setActionCommand(CHOOSE_IMAGES_DIR);
		mLWindow.getBtChooseImagesDir().addActionListener(this);
		
		mLWindow.getBtChooseDataDir().setActionCommand(CHOOSE_TRAINING_SET_DIR);
		mLWindow.getBtChooseDataDir().addActionListener(this);
		
		mLWindow.getBtChooseClassifierDir().setActionCommand(CHOOSE_CLASSIFIER_DIR);
		mLWindow.getBtChooseClassifierDir().addActionListener(this);
		
		mLWindow.getBtGenerateTrainingSet().setActionCommand(GENERATE_TRAINING_SET);
		mLWindow.getBtGenerateTrainingSet().addActionListener(this);
		
		mLWindow.getBtLoadTrainingSet().setActionCommand(LOAD_TRAINING_SET);
		mLWindow.getBtLoadTrainingSet().addActionListener(this);
		
		mLWindow.getBtTrainClassifier().setActionCommand(TRAIN_CLASSIFIER);
		mLWindow.getBtTrainClassifier().addActionListener(this);
		
	}
	
	
	private File loadFile(String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("./"));
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setVisible(true);
		if (chooser.showOpenDialog(chooser) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

}