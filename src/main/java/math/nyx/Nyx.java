package math.nyx;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import math.nyx.core.Collage;
import math.nyx.core.Matrix;
import math.nyx.core.imgproc.Image;
import math.nyx.fie.FractalImageEncodingStrategy;
import math.nyx.fie.simple.SimpleEncodingStrategy;
import math.nyx.util.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JTabbedPane;
import java.awt.Font;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JSlider;

public class Nyx extends JFrame {
	private static final long serialVersionUID = 642379045770710138L;

	private static Log LOG = LogFactory.getLog(Nyx.class);

	private FractalImageEncodingStrategy fic = new SimpleEncodingStrategy(0.1, 40);

	private JPanel outerPane;
	private Image originalImg, decodedImg;
	private ImageIcon originalImgIcon, decodedImgIcon;
	private JTextField txtThreshold;
	private JTextField txtIterations;
	private Collage imgAsTransform;

	public Nyx() {
		initUI();
    }

	public final void initUI() {
		Container contentPane =  getContentPane();
		contentPane.setLayout(new FlowLayout());

		outerPane = new JPanel();
		getContentPane().add(outerPane);

		originalImgIcon = new ImageIcon(new BufferedImage(256,256,BufferedImage.TYPE_BYTE_GRAY));

		decodedImgIcon = new ImageIcon(new BufferedImage(256,256,BufferedImage.TYPE_BYTE_GRAY));
				outerPane.setLayout(new BorderLayout(0, 0));
				
				JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
				outerPane.add(tabbedPane, BorderLayout.SOUTH);
						
						JPanel imageTabPane = new JPanel();
						tabbedPane.addTab("Images", null, imageTabPane, null);
						imageTabPane.setLayout(new BorderLayout(0, 0));
						
						JPanel imagePane = new JPanel();
						imageTabPane.add(imagePane, BorderLayout.CENTER);
						
						Component horizontalStrut_1 = Box.createHorizontalStrut(20);
						imagePane.add(horizontalStrut_1);
						
						JPanel encImgPane = new JPanel();
						imagePane.add(encImgPane);
						encImgPane.setLayout(new BorderLayout(0, 0));
						
						JLabel encImgTitle = new JLabel("Original Image");
						encImgTitle.setFont(encImgTitle.getFont().deriveFont(encImgTitle.getFont().getStyle() | Font.BOLD));
						encImgTitle.setHorizontalAlignment(SwingConstants.CENTER);
						encImgPane.add(encImgTitle, BorderLayout.NORTH);
						JLabel orginalImgLabel = new JLabel(originalImgIcon);
						orginalImgLabel.setText("");
						encImgPane.add(orginalImgLabel);
						
						Component horizontalStrut = Box.createHorizontalStrut(20);
						imagePane.add(horizontalStrut);
						
						JPanel decImgPane = new JPanel();
						imagePane.add(decImgPane);
						decImgPane.setLayout(new BorderLayout(0, 0));
						
						JLabel decImgTitle = new JLabel("Decoded Image");
						decImgTitle.setFont(decImgTitle.getFont().deriveFont(decImgTitle.getFont().getStyle() | Font.BOLD));
						decImgTitle.setHorizontalAlignment(SwingConstants.CENTER);
						decImgPane.add(decImgTitle, BorderLayout.NORTH);
						JLabel decodedImgLabel = new JLabel(decodedImgIcon);
						decImgPane.add(decodedImgLabel, BorderLayout.CENTER);
						
						Component horizontalStrut_2 = Box.createHorizontalStrut(20);
						imagePane.add(horizontalStrut_2);
						
						JPanel controlPane = new JPanel();
						imageTabPane.add(controlPane, BorderLayout.SOUTH);
								controlPane.setLayout(new BorderLayout(0, 0));
								
								JPanel westControlPane = new JPanel();
								controlPane.add(westControlPane, BorderLayout.WEST);
								
								Component horizontalStrut_3 = Box.createHorizontalStrut(20);
								westControlPane.add(horizontalStrut_3);
						
								JButton btnEncode = new JButton("Encode");
								westControlPane.add(btnEncode);
								btnEncode.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent event) {
										encodeDecode();
									}
								});
								
								JPanel centerControlPane = new JPanel();
								controlPane.add(centerControlPane, BorderLayout.CENTER);
								
								JButton btnDecode = new JButton("Decode");
								centerControlPane.add(btnDecode);
								btnDecode.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent event) {
										decodeImage();
									}
								});

								JButton btnNewButton_2 = new JButton("Animate");
								centerControlPane.add(btnNewButton_2);
								
								JPanel eastControlPane = new JPanel();
								controlPane.add(eastControlPane, BorderLayout.EAST);
								
								JButton btnNewButton = new JButton("Browse");
								eastControlPane.add(btnNewButton);
								
								JButton btnNewButton_1 = new JButton("Save");
								eastControlPane.add(btnNewButton_1);
								
								Component horizontalStrut_4 = Box.createHorizontalStrut(20);
								eastControlPane.add(horizontalStrut_4);
						
						JPanel partitionTabPane = new JPanel();
						tabbedPane.addTab("Partitions", null, partitionTabPane, null);
						partitionTabPane.setLayout(new BorderLayout(0, 0));
						
						JPanel partitionControlPane = new JPanel();
						partitionTabPane.add(partitionControlPane, BorderLayout.NORTH);
						
						JLabel lblPartitionControl = new JLabel("Partition");
						partitionControlPane.add(lblPartitionControl);
						
						JSlider partitionSlider = new JSlider();
						partitionSlider.setValue(1);
						partitionSlider.setMinimum(1);
						partitionSlider.setMaximum(99);
						partitionControlPane.add(partitionSlider);
						
						JLabel lblPartitionSelection = new JLabel("1 out of 99");
						partitionControlPane.add(lblPartitionSelection);
						
						JPanel partitionPane = new JPanel();
						partitionTabPane.add(partitionPane, BorderLayout.CENTER);
						
						Component horizontalStrut_5 = Box.createHorizontalStrut(20);
						partitionPane.add(horizontalStrut_5);
						
						JPanel rangeImgPane = new JPanel();
						partitionPane.add(rangeImgPane);
						rangeImgPane.setLayout(new BorderLayout(0, 0));
						
						JLabel lblRangeTitle = new JLabel("Range");
						rangeImgPane.add(lblRangeTitle, BorderLayout.NORTH);
						
						JLabel lblRangeImg = new JLabel("");
						rangeImgPane.add(lblRangeImg, BorderLayout.CENTER);
						
						Component horizontalStrut_6 = Box.createHorizontalStrut(20);
						partitionPane.add(horizontalStrut_6);
						
						JPanel domainImgPane = new JPanel();
						partitionPane.add(domainImgPane);
						domainImgPane.setLayout(new BorderLayout(0, 0));
						
						JLabel lblDomainTitle = new JLabel("Domain");
						domainImgPane.add(lblDomainTitle, BorderLayout.NORTH);
						
						JLabel lblDomainImg = new JLabel("");
						domainImgPane.add(lblDomainImg, BorderLayout.CENTER);
						
						Component horizontalStrut_7 = Box.createHorizontalStrut(20);
						partitionPane.add(horizontalStrut_7);
						
						JPanel optionPanel = new JPanel();
						tabbedPane.addTab("Options", null, optionPanel, null);
						GridBagLayout gbl_optionPanel = new GridBagLayout();
						gbl_optionPanel.columnWidths = new int[]{100, 0, 46, 86, 1, 1, 46, 0};
						gbl_optionPanel.rowHeights = new int[]{20, 0, 0, 0};
						gbl_optionPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
						gbl_optionPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
						optionPanel.setLayout(gbl_optionPanel);
						
						JLabel lblThreshold = new JLabel("Threshold");
						GridBagConstraints gbc_lblThreshold = new GridBagConstraints();
						gbc_lblThreshold.anchor = GridBagConstraints.EAST;
						gbc_lblThreshold.insets = new Insets(0, 0, 5, 5);
						gbc_lblThreshold.gridx = 0;
						gbc_lblThreshold.gridy = 1;
						optionPanel.add(lblThreshold, gbc_lblThreshold);
						
						txtThreshold = new JTextField();
						txtThreshold.setText("0.1");
						GridBagConstraints gbc_txtThreshold = new GridBagConstraints();
						gbc_txtThreshold.anchor = GridBagConstraints.NORTHWEST;
						gbc_txtThreshold.insets = new Insets(0, 0, 5, 5);
						gbc_txtThreshold.gridx = 1;
						gbc_txtThreshold.gridy = 1;
						optionPanel.add(txtThreshold, gbc_txtThreshold);
						txtThreshold.setColumns(10);
						
						JLabel lblIterations = new JLabel("Iterations");
						GridBagConstraints gbc_lblIterations = new GridBagConstraints();
						gbc_lblIterations.anchor = GridBagConstraints.EAST;
						gbc_lblIterations.insets = new Insets(0, 0, 0, 5);
						gbc_lblIterations.gridx = 0;
						gbc_lblIterations.gridy = 2;
						optionPanel.add(lblIterations, gbc_lblIterations);
						
						txtIterations = new JTextField();
						txtIterations.setText("10");
						GridBagConstraints gbc_txtIterations = new GridBagConstraints();
						gbc_txtIterations.anchor = GridBagConstraints.WEST;
						gbc_txtIterations.insets = new Insets(0, 0, 0, 5);
						gbc_txtIterations.gridx = 1;
						gbc_txtIterations.gridy = 2;
						optionPanel.add(txtIterations, gbc_txtIterations);
						txtIterations.setColumns(10);

		setTitle("Nyx - Fractal Image Encoding");
		setSize(725, 450);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public void setOriginalImage(Image img) {
		originalImg = img.toGrayscale();
		originalImgIcon.setImage(originalImg);
		setDecodedImage(new Image(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY));
	}

	public void setDecodedImage(Image img) {
		decodedImg = img;
		decodedImgIcon.setImage(decodedImg);
		outerPane.revalidate();
	}

	public void encodeDecode() {
		LOG.info(String.format("Converting %dx%d image to matrix...", originalImg.getWidth(), originalImg.getHeight()));
		Matrix m = new Matrix(originalImg);
		setOriginalImage(new Image(m));

		LOG.info("Encoding image to transform...");
		imgAsTransform = fic.encode(m);
		LOG.info("Done encoding image.");
		decodeImage();
	}

	private void decodeImage() {
		LOG.info("Decoding image from transform at original resolution...");
		Matrix mm = fic.decode(imgAsTransform, originalImg.getWidth(), originalImg.getHeight(), 20);
		setDecodedImage(new Image(mm));
		LOG.info("Done decoding.");
		this.setVisible(false);
		this.setVisible(true);
	}

	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        		Image img = null;
        		try {
        			Resource imgFile = new ClassPathResource("math/nyx/resources/lena_256.jpg");
        		    img = ImageIO.read(imgFile.getInputStream());
        		} catch (IOException e) {
        			LOG.error(String.format("Failed to open image file: %s. Exiting.", e));
        			return;
        		}

            	Nyx nyx = new Nyx();
            	nyx.setOriginalImage(img);
            	nyx.setVisible(true);
            }
        });
    }
}
