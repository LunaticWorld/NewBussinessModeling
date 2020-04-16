package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EnumSet;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.javadocking.DockingExecutor;
import com.javadocking.DockingManager;
import com.javadocking.dock.BorderDock;
import com.javadocking.dock.FloatDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.TabDock;
import com.javadocking.dock.docker.BorderDocker;
import com.javadocking.dock.factory.LeafDockFactory;
import com.javadocking.dock.factory.ToolBarDockFactory;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.DockingMode;
import com.javadocking.dockable.DraggableContent;
import com.javadocking.dockable.StateActionDockable;
import com.javadocking.dockable.action.DefaultDockableStateActionFactory;
import com.javadocking.drag.DragListener;
import com.javadocking.event.DockingEvent;
import com.javadocking.event.DockingListener;
import com.javadocking.model.DefaultDockingPath;
import com.javadocking.model.DockingPath;
import com.javadocking.model.FloatDockModel;
import com.javadocking.visualizer.DockingMinimizer;
import com.javadocking.visualizer.FloatExternalizer;
import com.javadocking.visualizer.SingleMaximizer;

public class UClairAnalyzer extends JPanel implements ActionListener {

	/**
	 * 
	 */

	// Static fields.

	private static final long serialVersionUID = 1L;
	public static final int FRAME_WIDTH = 900;
	public static final int FRAME_HEIGHT = 600;

	// Create Values

	FloatDockModel dockModel;

	TextPanel textPanel1;
	TextPanel textPanel2;
	TextPanel textPanel3;
	TextPanel textPanel4;

	Dockable dockable_JTree;
	
	ContactTree jtree_panel = new ContactTree();
	
	Dockable dockable_Analysis_Invisible;	// 추가될 창 위치를 갖고 보이지는 않는다.
	Dockable dockable3;
	Dockable dockable4;

	TabDock leftTabDock;
	TabDock centerTabDock;
	TabDock bottomTabDock;

	SplitDock topSplitDock;
	SplitDock bottomSplitDock;
	SplitDock totalSplitDock;

	JMenuBar menuBar;
	
	DockingPath dockingPath;

	/**
	 * 메뉴 목록 ( 파일, 분석, 테스트케이스, 도움말 )
	 */
	JMenu m_file;
	JMenu m_analysis;
	JMenu m_testcase;
	JMenu m_help;

	/**
	 * 파일 메뉴 하위목록
	 */
	JMenuItem mi_proj_open;
	JMenuItem mi_proj_close;
	JMenuItem mi_prog_exit;

	/**
	 * 분석 메뉴 하위목록
	 */
	public enum Analysis_Selector {
		NonExistTag, VirtualTag, PhysicalAddress, ObjConnTag, Event, CalcScript, ObjEffectCompatibility, None
	}

	Analysis_Selector analySelector = Analysis_Selector.None;

	JMenuItem mi_analy_nonexistTag;
	JMenuItem mi_analy_virtualTag;
	JMenuItem mi_analy_physicalAddress;
	JMenuItem mi_analy_objconnTag;
	JMenuItem mi_analy_event;
	JMenuItem mi_analy_calcScript;
	JMenuItem mi_analy_objeffectCompatibility;

	HashMap<Analysis_Selector, Dockable> dock_hashmap_array = new HashMap<Analysis_Selector, Dockable>();
	String tableName = "";
	
	/**
	 * 테스트케이스 메뉴 하위목록
	 */
	JMenuItem mi_testcase_ioTestcaseCreate;
	JMenuItem mi_testcase_monitorTestCaseCreate;

	/**
	 * 도움말 메뉴 하위목록
	 */
	JMenuItem mi_help_showContents;
	JMenuItem mi_help_infoEditorInfo;
	JMenuItem mi_help_systemEditorInfo;

	// Constructor.

	public UClairAnalyzer(JFrame frame) {
		super(new BorderLayout());

		// 독 모델을 생성
		dockModel = new FloatDockModel();
		dockModel.addOwner("frame0", frame);
		
		// 독 모델에 기능을 넣어줌
		DockingManager.setDockModel(dockModel);

		// 컴포넌트 생성
		textPanel1 = new TextPanel("");
		textPanel2 = new TextPanel("This is invisible Window.");
		textPanel3 = new TextPanel("");
		textPanel4 = new TextPanel("");

		// 패널을 독기능을 넣어주고 독설정을 해준다
		dockable_JTree = new DefaultDockable("Window1", jtree_panel, "분석기", null, DockingMode.ALL);
		dockable_Analysis_Invisible = new DefaultDockable("Window2", textPanel2, "분석기 창이 나올 위치", null, DockingMode.ALL);
		dockable3 = new DefaultDockable("Window3", textPanel3, "System out", null, DockingMode.ALL);
		dockable4 = new DefaultDockable("Window4", textPanel4, "System err", null, DockingMode.ALL);

		dockable_JTree = addActions(dockable_JTree);
		dockable_Analysis_Invisible = addActions(dockable_Analysis_Invisible);
		dockable3 = addActions(dockable3);
		dockable4 = addActions(dockable4);

		FloatDock floatDock = dockModel.getFloatDock(frame);
		floatDock.setChildDockFactory(new LeafDockFactory(false));

		// 탭 도크 생성
		leftTabDock = new TabDock();
		centerTabDock = new TabDock();		
		bottomTabDock = new TabDock();

		// 탭 도크에 독테이블을 추가
		leftTabDock.addDockable(dockable_JTree, new Position(0));
		centerTabDock.addDockable(dockable_Analysis_Invisible, new Position(0));
		centerTabDock.setSelectedDockable(dockable_Analysis_Invisible);

		bottomTabDock.addDockable(dockable3, new Position(0));
		bottomTabDock.addDockable(dockable4, new Position(1));

		// 윈도우의 탭독 구간을 나눌수 있게 만들어줌
		// 구간 독 에 탭 도크를 놓을수 있게 만든다.

		topSplitDock = new SplitDock();
		topSplitDock.addChildDock(leftTabDock, new Position(Position.LEFT));
		topSplitDock.addChildDock(centerTabDock, new Position(Position.RIGHT));
		topSplitDock.setDividerLocation(180);

		bottomSplitDock = new SplitDock();
		bottomSplitDock.addChildDock(bottomTabDock, new Position(Position.CENTER));

		totalSplitDock = new SplitDock();
		totalSplitDock.addChildDock(topSplitDock, new Position(Position.TOP));
		totalSplitDock.addChildDock(bottomSplitDock, new Position(Position.BOTTOM));
		totalSplitDock.setDividerLocation(300);

		
		// 도크 모델에 올려놓음
		dockModel.addRootDock("totalDock", totalSplitDock, frame);

		
		/*
		 * 도킹창 최소화, 최대화기능을 수행하기 위한 코드
		 * 
		 */
		SingleMaximizer maximizePanel = new SingleMaximizer(totalSplitDock);
		dockModel.addVisualizer("maximizer", maximizePanel, frame);

		BorderDock minimizerBorderDock = new BorderDock(new ToolBarDockFactory());
		minimizerBorderDock.setMode(BorderDock.MODE_MINIMIZE_BAR);
		minimizerBorderDock.setCenterComponent(maximizePanel);
		BorderDocker borderDocker = new BorderDocker();
		borderDocker.setBorderDock(minimizerBorderDock);
		DockingMinimizer minimizer = new DockingMinimizer(borderDocker);
		dockModel.addVisualizer("minimizer", minimizer, frame);

		dockModel.addVisualizer("externalizer", new FloatExternalizer(frame), frame);

		// root 도커와 같은 위치에 추가한다.
		dockModel.addRootDock("minimizerBorderDock", minimizerBorderDock, frame);

		
		// 패널에 스플릿 패널 추가
		add(minimizerBorderDock, BorderLayout.CENTER);

		/*
		 * 도킹창들에 대한 path를 추가한다. 이후 dockingPath를 통해 창이 추가될 위치를 고정한다.
		 */
		addDockingPath(dockable_JTree);
		addDockingPath(dockable_Analysis_Invisible);
		addDockingPath(dockable3);
		addDockingPath(dockable4);
		
		dockingPath = DockingManager.getDockingPathModel().getDockingPath(dockable_Analysis_Invisible.getID());

		centerTabDock.setVisible(false);
		
		
		//---------------------------Menu--------------------------

		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		m_file = new JMenu("파일");
		m_analysis = new JMenu("분석");
		m_testcase = new JMenu("테스트 케이스");
		m_help = new JMenu("도움말");

		menuBar.add(m_file);
		menuBar.add(m_analysis);
		menuBar.add(m_testcase);
		menuBar.add(m_help);

		mi_proj_open = new JMenuItem("프로젝트 열기");
		mi_proj_close = new JMenuItem("프로젝트 닫기");
		mi_prog_exit = new JMenuItem("종료", KeyEvent.VK_F4);

		mi_proj_open
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK));
		mi_proj_open.getAccessibleContext().setAccessibleDescription("프로젝트 열기");
		mi_proj_open.addActionListener(this);

		mi_proj_close
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK));

		mi_prog_exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		mi_prog_exit.getAccessibleContext().setAccessibleDescription("Exit the application");
		mi_prog_exit.addActionListener(this);

		m_file.add(mi_proj_open);
		m_file.add(mi_proj_close);
		m_file.addSeparator();
		m_file.add(mi_prog_exit);

		mi_analy_nonexistTag = new JMenuItem("존재하지 않는 태그 분석");
		mi_analy_virtualTag = new JMenuItem("가상태그 종속성 분석");
		mi_analy_physicalAddress = new JMenuItem("물리주소 종속성 분석");
		mi_analy_objconnTag = new JMenuItem("객체태그 연결정보 분석");
		mi_analy_event = new JMenuItem("이벤트 종속성 분석");
		mi_analy_calcScript = new JMenuItem("계산 스크립트 검증");
		mi_analy_objeffectCompatibility = new JMenuItem("객체효과 양립성 분석");

		mi_analy_nonexistTag.addActionListener(this);
		mi_analy_virtualTag.addActionListener(this);
		mi_analy_physicalAddress.addActionListener(this);
		mi_analy_objconnTag.addActionListener(this);
		mi_analy_event.addActionListener(this);
		mi_analy_calcScript.addActionListener(this);
		mi_analy_objeffectCompatibility.addActionListener(this);

		m_analysis.add(mi_analy_nonexistTag);
		m_analysis.add(mi_analy_virtualTag);
		m_analysis.add(mi_analy_physicalAddress);
		m_analysis.add(mi_analy_objconnTag);
		m_analysis.add(mi_analy_event);
		m_analysis.add(mi_analy_calcScript);
		m_analysis.add(mi_analy_objeffectCompatibility);

		mi_testcase_ioTestcaseCreate = new JMenuItem("IO 테스트 케이스 생성기");
		mi_testcase_monitorTestCaseCreate = new JMenuItem("화면 테스트 케이스 생성기");

		m_testcase.add(mi_testcase_ioTestcaseCreate);
		m_testcase.add(mi_testcase_monitorTestCaseCreate);

		mi_help_showContents = new JMenuItem("도움말 보기(목차)");
		mi_help_infoEditorInfo = new JMenuItem("정보 편집기 정보");
		mi_help_systemEditorInfo = new JMenuItem("시스템 편집기 정보");

		mi_help_showContents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));

		m_help.add(mi_help_showContents);
		m_help.addSeparator();
		m_help.add(mi_help_infoEditorInfo);
		m_help.add(mi_help_systemEditorInfo);
	}

	/**
	 * implements ActionListener --> called,  메뉴 클릭시 종료 기능과 같은 이벤트를 한곳에서 처리합니다.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		analySelector = Analysis_Selector.None;
		if (e.getSource() == mi_prog_exit) {
			System.exit(0);
		} else if (e.getSource() == mi_analy_nonexistTag) {
			analySelector = Analysis_Selector.NonExistTag;
		} else if (e.getSource() == mi_analy_virtualTag) {
			analySelector = Analysis_Selector.VirtualTag;
		} else if (e.getSource() == mi_analy_physicalAddress) {
			analySelector = Analysis_Selector.PhysicalAddress;
		} else if (e.getSource() == mi_analy_objconnTag) {
			analySelector = Analysis_Selector.ObjConnTag;
		} else if (e.getSource() == mi_analy_event) {
			analySelector = Analysis_Selector.Event;
		} else if (e.getSource() == mi_analy_calcScript) {
			analySelector = Analysis_Selector.CalcScript;
		} else if (e.getSource() == mi_analy_objeffectCompatibility) {
			analySelector = Analysis_Selector.ObjEffectCompatibility;
		} else if (e.getSource() == mi_proj_open) {
			openProjFile();
		}

		callAnalysisWindow(analySelector);
	}

	private void openProjFile() {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.showDialog(this, null);

		String folderName = jfc.getSelectedFile().getPath();
		
		int pos = folderName.lastIndexOf( "\\" );
		String ext = folderName.substring( pos + 1 );
		
		System.out.println(ext);
		
		for(Analysis_Selector analysis: Analysis_Selector.values()) {
			if(dock_hashmap_array.containsKey(analysis)) {

				//DockingManager.getDockingExecutor().changeDocking(dock_hashmap_array.get(analysis), dockingPath);
				leftTabDock.removeDockable(dock_hashmap_array.get(analysis));
				bottomTabDock.removeDockable(dock_hashmap_array.get(analysis));
				centerTabDock.removeDockable(dock_hashmap_array.get(analysis));
				
			}
		}
		jtree_panel.setFolderName(ext);
		
		invalidate();
		repaint();
	}

	/**
	 * 분석과 관련된 창 생성 또는 포커스기능을 한다.
	 * 
	 * @param Analysis_Selector
	 */
	private void callAnalysisWindow(Analysis_Selector select) {
		
		if(select == Analysis_Selector.None) return;
		
		System.out.println("------------분석창 호출됨--------------");
		
		// Create the Table.
		JPanel tablePanel = new Table(8); 
		
		// Create the dockable for the Table.
		switch (select) {
		case NonExistTag:
			tableName = mi_analy_nonexistTag.getText();
			break;
		case VirtualTag:
			tableName = mi_analy_virtualTag.getText();
			break;
		case PhysicalAddress:
			tableName = mi_analy_physicalAddress.getText();
			break;
		case ObjConnTag:
			tableName = mi_analy_objconnTag.getText();
			break;
		case Event:
			tableName = mi_analy_event.getText();
			break;
		case CalcScript:
			tableName = mi_analy_calcScript.getText();
			break;
		case ObjEffectCompatibility:
			tableName = mi_analy_objeffectCompatibility.getText();
			break;
			default:
				tableName = "";
				break;
		}
		
		if(tableName.equals("")) return;
		
		if(dock_hashmap_array.containsKey(select)) {
			System.out.println(dock_hashmap_array.get(select));
			
			/**
			 * TODO changeDocking으로 창 포커스 바꾸는건 좋은데, 이거로 관련해서 버그가 많다. 나중에 찾아봐얄듯
			 * 
			 * 	버그 하나더, 창 최소화시 창이 deleted판정을 받아서 창이 사라진것으로 판단함.
			 */
			
			// 창 포커스는 이거로 넘기면 된다. 이거덕분에 시간 꽤나 먹었다.
			DockingManager.getDockingExecutor().changeDocking(dock_hashmap_array.get(select), dockingPath);
			
		}else {
						
			int dialogResult = JOptionPane.showConfirmDialog (null, tableName +"을(를) 실행하시겠습니까?","Confirm",JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.NO_OPTION){
			  return;
			}
			
			Dockable dockable = createDockable(tableName, tablePanel, tableName,  null, tableName);
			
			
			dock_hashmap_array.put(select, dockable);
			
			// Where do we want the dockable to be placed?
			//DockingPath newDockingPath = DefaultDockingPath.copyDockingPath(dockable, dockingPath);
			//DockingManager.getDockingPathModel().add(newDockingPath);
			
			
			// Add the dockable.
			DockingManager.getDockingExecutor().changeDocking(dockable, dockingPath);
			
			dockable.getContent().getParent().setFocusable(true);
			
			dockable.addDockingListener(new DockingListener() {
				
				@Override
				public void dockingWillChange(DockingEvent e) {
				}
				
				@Override
				public void dockingChanged(DockingEvent e) {
					if(e.getDestinationDock() != null) {
						dock_hashmap_array.put(select, dockable);
						System.out.println("added : "+dockable);
					}else {
						dock_hashmap_array.remove(select);
						System.out.println("deleted : "+dockable);
					}
				}
			});
			
			centerTabDock.removeDockable(dockable_Analysis_Invisible);
			centerTabDock.setVisible(true);
		}
	}
	
	/**
	 * 동적으로 도킹창 생성을 위한 코드이다.
	 * 
	 * Create a dockable for a given content component.
	 * 
	 * @param 	id 		The ID of the dockable. The IDs of all dockables should be different.
	 * @param 	content The content of the dockable. 
	 * @param 	title 	The title of the dockable.
	 * @param 	icon 	The icon of the dockable.
	 * @return			The created dockable.
	 * @throws 	IllegalArgumentException	If the given ID is null.
	 */
	private Dockable createDockable(String id, JPanel content, String title, Icon icon, String description) {
		
		// Create the dockable.
		DefaultDockable dockable = new DefaultDockable(id, content, title, icon);
		
		// Add a description to the dockable. It will be displayed in the tool tip.
		dockable.setDescription(description);
		
		Dockable dockableWithActions = addAllActions(dockable);
		
		return dockableWithActions;
	}

	private Dockable addAllActions(DefaultDockable dockable) {
		
		Dockable wrapper = new StateActionDockable(dockable, new DefaultDockableStateActionFactory(), DockableState.statesClosed());
		wrapper = new StateActionDockable(wrapper, new DefaultDockableStateActionFactory(), DockableState.statesAllExceptClosed());
		return wrapper;
	}
	
	
	/**
	 *	JTable 생성을 위한 클래스이다.
	 */
	public class Table extends JPanel
	{
		// Static fields.

		public static final int LIST = 0;
		public static final int TABLE = 1;
		
		// Fields.

		private int tableSize = TABLE;
		
		// Constructors.

		public Table(int tableSize)
		{
			super(new BorderLayout());

			this.tableSize = tableSize; 
			
			MyTableModel dataModel = new MyTableModel();
			JTable table = new JTable(dataModel);
			JScrollPane scrollpane = new JScrollPane(table);
			add(scrollpane, BorderLayout.CENTER);

		}

		// Getters / Setters.

		public int getTableSize()
		{
			return tableSize;
		}

		public void setTableSize(int size)
		{
			this.tableSize = size;
		}

		/**
		 * 안에 코드가 그냥 테이블사이즈가 0이면 1x20, 그외면 4x4 테이블을 만든다. 이후 수정이 필요
		 */
		private class MyTableModel extends AbstractTableModel
		{
			public int getColumnCount()
			{
				if (tableSize == LIST)
				{
					return 1;
				}
				return 4;
			}

			public int getRowCount()
			{
				if (tableSize == LIST)
				{
					return 20;
				}
				return 4;
			}

			public Object getValueAt(int row, int col)
			{
				return "Hello";
			}
		}
	}
	
	/**
	 * 
	 * 도킹 창에대한 path를 추가하기위한 메소드이다.
	 * 
	 * @param dockable
	 * @return DockingPath
	 */

	private DockingPath addDockingPath(Dockable dockable) {
		
		if (dockable.getDock() != null) {
			// Create the docking path of the dockable.
			DockingPath dockingPath = DefaultDockingPath.createDockingPath(dockable);
			DockingManager.getDockingPathModel().add(dockingPath);
			return dockingPath;
		}

		return null;
	}

	/**
	 * Docking창에 최소화, 최대화, 창분리 버튼을 생성함
	 * 
	 * @param dockable
	 * @return
	 */
	private Dockable addActions(Dockable dockable) {

		// Decorate with state actions.
		Dockable wrapper = new StateActionDockable(dockable, new DefaultDockableStateActionFactory(), new int[0]);
		int[] states = { DockableState.NORMAL, DockableState.MINIMIZED, DockableState.MAXIMIZED,
				DockableState.EXTERNALIZED };
		wrapper = new StateActionDockable(wrapper, new DefaultDockableStateActionFactory(), states);

		return wrapper;
	}

	/**
	 * 창 아무데나 드래그하여 창 이동을 할 수 있는 JLabel을 상속받은 TextPanel 클래스이다.
	 */
	private class TextPanel extends JPanel implements DraggableContent {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JLabel label;

		public TextPanel(String text) {
			super(new FlowLayout());

			// The panel.
			setMinimumSize(new Dimension(100, 100));
			setPreferredSize(new Dimension(150, 150));
			setBackground(Color.white);

			setBorder(BorderFactory.createLineBorder(Color.white));

			// The label.
			label = new JLabel(text);
			label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			add(label);
		}

		// Implementations of DraggableContent.

		public void addDragListener(DragListener dragListener) {
			addMouseListener(dragListener);
			addMouseMotionListener(dragListener);
			label.addMouseListener(dragListener);
			label.addMouseMotionListener(dragListener);
		}
	}
	
	/**
	 * 트리를 생성하는 클래스, JPanel로 묶어 클래스화했다.
	 *
	 */
	private class ContactTree extends JPanel implements MouseListener {

		DefaultMutableTreeNode analy_nonexistTag_Node;
		DefaultMutableTreeNode analy_virtualTag_Node;
		DefaultMutableTreeNode analy_physicalAddress_Node;
		DefaultMutableTreeNode analy_objconnTag_Node;
		DefaultMutableTreeNode analy_event_Node;
		DefaultMutableTreeNode analy_calcScript_Node;
		DefaultMutableTreeNode analy_objeffectCompatibility_Node;

		DefaultMutableTreeNode rootNode;

		DefaultTreeModel treeModel;

		JTree tree;

		public ContactTree() {

			analy_nonexistTag_Node = new DefaultMutableTreeNode("존재하지 않는 태그 분석");
			analy_virtualTag_Node = new DefaultMutableTreeNode("가상태그 종속성 분석");
			analy_physicalAddress_Node = new DefaultMutableTreeNode("물리주소 종속성 분석");
			analy_objconnTag_Node = new DefaultMutableTreeNode("객체태그 연결정보 분석");
			analy_event_Node = new DefaultMutableTreeNode("이벤트 종속성 분석");
			analy_calcScript_Node = new DefaultMutableTreeNode("계산 스크립트 검증");
			analy_objeffectCompatibility_Node = new DefaultMutableTreeNode("객체효과 양립성 분석");
				
			rootNode = new DefaultMutableTreeNode("Monitoring");
			rootNode.add(analy_nonexistTag_Node);
			rootNode.add(analy_virtualTag_Node);
			rootNode.add(analy_physicalAddress_Node);
			rootNode.add(analy_objconnTag_Node);
			rootNode.add(analy_event_Node);
			rootNode.add(analy_calcScript_Node);
			rootNode.add(analy_objeffectCompatibility_Node);

			// Create the tree model.
			treeModel = new DefaultTreeModel(rootNode);

			
			// Create the JTree from the tree model.
			tree = new JTree(treeModel);
			
			// Expand the tree.
			for (int row = 0; row < tree.getRowCount(); row++) {
				tree.expandRow(row);
			}

			tree.addMouseListener(this);

			// Add the tree in a scroll pane.
			setLayout(new BorderLayout());
			add(new JScrollPane(tree), BorderLayout.CENTER);

		}
		
		public void setFolderName(String folderName) {
			rootNode.setUserObject(folderName);
			treeModel.nodeChanged(rootNode);
			
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			int selRow = tree.getRowForLocation(e.getX(), e.getY());
			TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

			if (selRow != -1) {

				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

				if (selectedNode == analy_nonexistTag_Node) {
					analySelector = Analysis_Selector.NonExistTag;
				} else if (selectedNode == analy_virtualTag_Node) {

					analySelector = Analysis_Selector.VirtualTag;
				} else if (selectedNode == analy_physicalAddress_Node) {
					analySelector = Analysis_Selector.PhysicalAddress;
				} else if (selectedNode == analy_objconnTag_Node) {
					analySelector = Analysis_Selector.ObjConnTag;
				} else if (selectedNode == analy_event_Node) {
					analySelector = Analysis_Selector.Event;
				} else if (selectedNode == analy_calcScript_Node) {
					analySelector = Analysis_Selector.CalcScript;
				} else if (selectedNode == analy_objeffectCompatibility_Node) {
					analySelector = Analysis_Selector.ObjEffectCompatibility;
				} else {
					analySelector = Analysis_Selector.None;
				}

				if (e.getClickCount() == 1) {
					
				} else if (e.getClickCount() == 2) {

					callAnalysisWindow(analySelector);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

	}
	/**
	 *  ----------------------------------------Main method.----------------------
	 */
	public static void createAndShowGUI() {

		// Create the frame.
		JFrame frame = new JFrame("UClair Analyzer");
		// Create the panel and add it to the frame.
		UClairAnalyzer panel = new UClairAnalyzer(frame);
		frame.getContentPane().add(panel);

		// Set the frame properties and show it.

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((screenSize.width - FRAME_WIDTH) / 2, (screenSize.height - FRAME_HEIGHT) / 2);
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String args[]) {
		Runnable doCreateAndShowGUI = new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		};
		SwingUtilities.invokeLater(doCreateAndShowGUI);
	}
}