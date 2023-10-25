package org.vanted.addons.matrix.writing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import org.AttributeHelper;
import org.ErrorMsg;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.graffiti.editor.MainFrame;
import org.vanted.addons.matrix.mapping.DataPathway;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class MatrixDataFileWriter {
	
	private XSSFWorkbook xssfWorkbook;
	private XSSFSheet xssfWorksheet;
	private XSSFCreationHelper xssfCreationHelper;
	private XSSFCellStyle cellStyleHeadline;
	private XSSFCellStyle cellStyleData;

	public MatrixDataFileWriter() {
		initHSSFObjects();
	}
	
	/**
	 * Creates global objects for excel-java interaction, for instance cell styling:
	 * color, font.
	 */
	private void initHSSFObjects() {

		xssfWorkbook = new XSSFWorkbook();
		xssfWorksheet = xssfWorkbook.createSheet("Experiment");
		xssfCreationHelper = xssfWorkbook.getCreationHelper();

		// cellStyleGrey = xssfWorkbook.createCellStyle();
		// fill pattern should be "1" but it doesn't work
		// XSSFCellStyle.SOLID_FOREGROUND is set to "1" but this seems to be wrong
		// cellStyleGrey.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		// cellStyleGrey.setFillPattern((short) 1);
		// cellStyleGrey.setFillBackgroundColor(new XSSFColor(new Color(228, 228,
		// 228)));

		// cellStyleYellow = xssfWorkbook.createCellStyle();
		// fill pattern should be "1" but it doesn't work
		// XSSFCellStyle.SOLID_FOREGROUND is set to "1" but this seems to be wrong
		// cellStyleYellow.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		// cellStyleYellow.setFillPattern((short) 1);
		// cellStyleGrey.setFillBackgroundColor(new XSSFColor(new Color(255, 255,
		// 204)));

		// cellStyleTurquoise = xssfWorkbook.createCellStyle();
		// fill pattern should be "1" but it doesn't work
		// XSSFCellStyle.SOLID_FOREGROUND is set to "1" but this seems to be wrong
		// cellStyleTurquoise.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		// cellStyleTurquoise.setFillPattern((short) 1);
		// cellStyleGrey.setFillBackgroundColor(new XSSFColor(new Color(204, 255,
		// 255)));

		cellStyleHeadline = xssfWorkbook.createCellStyle();
		cellStyleHeadline.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		XSSFFont font = xssfWorkbook.createFont();
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		cellStyleHeadline.setFont(font);

		// data style
		cellStyleData = xssfWorkbook.createCellStyle();
		cellStyleData.setAlignment(XSSFCellStyle.ALIGN_CENTER);

		CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 2);
		xssfWorksheet.addMergedRegion(cellRangeAddress);

	}
	
	/**
	 * Export the given {@link ExperimentInterface} to the given {@link File}.
	 * 
	 * @param excelfile
	 * @param md
	 * @return
	 */
	public static void writeExcel(File excelfile, ExperimentInterface md) {
		if (excelfile != null) {
			try {
				MatrixDataFileWriter edfw = new MatrixDataFileWriter();
				edfw.addHeader(md);
				edfw.addSampleHeader(md);
				
				edfw.addValues(md);
				
				edfw.write(excelfile);
			} catch (Exception err) {
				ErrorMsg.addErrorMessage(err);
			}
		}
	}
	
	private void addHeader(ExperimentInterface md){
		String title = "";
		if (MainFrame.getInstance() != null)
			title = " by " + MainFrame.getInstance().getTitle();
		XSSFRow row1 = xssfWorksheet.createRow((short) 0);
		XSSFCell cellA1 = row1.createCell(0, XSSFCell.CELL_TYPE_STRING);
		cellA1.setCellValue("Exported" + title + " on " + AttributeHelper.getDateString(new Date()));

		createHeadline(1, 0, "BIOCHEMICAL");
		createHeadline(1, 1, "SUPER_PATHWAY");
		createHeadline(1, 2, "SUB_PATHWAY");
		createHeadline(1, 3, "CAS");
		createHeadline(1, 4, "PUBCHEM");
		createHeadline(1, 5, "CHEMSPIDER");
		createHeadline(1, 6, "KEGG");
		createHeadline(1, 7, "HMDB");
	
	}
	
	private void addValues(ExperimentInterface md){
		ArrayList<DataPathway> pwList = new ArrayList<DataPathway>();
		int row = 2;
		int col = 0;
		for(SubstanceInterface substance: md) {
			SubstanceWithPathways substWPw = (SubstanceWithPathways) substance;
			
			String allNames = ""; // substWPw.getName();
			for(String altName: substWPw.getAlternativeNames()) {
				allNames = allNames + altName  + ";";
			}
			allNames = allNames.substring(0, allNames.lastIndexOf(";"));
			createCellIfNotExistsAndSet(row, col++, allNames, cellStyleData);
			
			
			ArrayList<DataPathway> pathwaysList = substWPw.getPathways();
			
			if(pathwaysList.size() > 0) {
				createCellIfNotExistsAndSet(row, col++, pathwaysList.get(0).getSuperPathway(), cellStyleData);
				
				String allPathways = "";
				for(DataPathway pathway: pathwaysList) {
					allPathways = allPathways + pathway.getTitle() + ";";
					
					if(!pwList.contains(pathway)) {
						pwList.add(pathway);
					}
					
				}
				allPathways = allPathways.substring(0, allPathways.lastIndexOf(";"));
				createCellIfNotExistsAndSet(row, col++, allPathways, cellStyleData);
			}else { //if the substance is currently not assigned a pathway
				createCellIfNotExistsAndSet(row, col++,"", cellStyleData);	//dummy super pathway
				createCellIfNotExistsAndSet(row, col++, "", cellStyleData);	// dummy pathway list
			}
			
			for(int index = 0 ;index < 5 ; index++) {
				createCellIfNotExistsAndSet(row, col++, substance.getSynonyme(index), cellStyleData);
			}
			
			for (ConditionInterface series : substWPw) {
				//col = 8;
				for (SampleInterface sample : series) {
					for (NumericMeasurementInterface meas : sample) {
						createCellIfNotExistsAndSet(row, col++, meas.getValue(), cellStyleData);
					}
				}
			}
			row++;
			col = 0;
		}
		
		row = row + 2;
		
		createCellIfNotExistsAndSet(row, 0, "PATHWAY", cellStyleHeadline);
		createCellIfNotExistsAndSet(row, 1, "SUPER PATHWAY", cellStyleHeadline);
		createCellIfNotExistsAndSet(row++, 2, "SCORE", cellStyleHeadline);
		
		for(DataPathway dpw: pwList) {
				if(dpw.getTitle() != null && dpw.getTitle().length() > 1) {
				// name
					createCellIfNotExistsAndSet(row, col++, dpw.getTitle(), cellStyleData);
				//superpw
					createCellIfNotExistsAndSet(row, col++, dpw.getSuperPathway(), cellStyleData);	
				//score	
					createCellIfNotExistsAndSet(row++, col, dpw.getScore(), cellStyleData);	
				}
			col = 0;
		}
	}

	private void addSampleHeader(ExperimentInterface md){
	
		SubstanceInterface substance = md.get(0);
		ArrayList<ConditionInterface> conditionList =  new ArrayList<ConditionInterface>(substance.getConditions(null));
		ConditionInterface series = conditionList.get(0);
				
		int col = 8;
		for(SampleInterface s: series) {
			for(int i = 0; i < s.size(); i++) {
				this.createHeadline(1, col++, new String(s.getTime() + s.getTimeUnit() + "No." + (i+1)));
			}	
		}
	}
	
	/**
	 * Creates a cell as headline. The Headline-Style is fix codes in method
	 * initHSSFObjects().
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 */
	private void createHeadline(int rowIndex, int columnIndex, String text) {
		XSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
		XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
		XSSFFont font = xssfWorkbook.createFont();
		font.setFontHeightInPoints((short) 12);
		font.setFontName("Gothic L");
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		cellStyle.setFont(font);
		cell.setCellStyle(cellStyle);
		autoFitCells();
	}
	
	/**
	 * creates the worksheet cell for given row and column index.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	private XSSFCell createCellIfNotExists(int rowIndex, int columnIndex) {
		XSSFRow row = createRowIfNotExists(rowIndex);
		XSSFCell cell = row.getCell(columnIndex);
		if (null == cell)
			cell = row.createCell(columnIndex);
		return cell;
	}
	
	/**
	 * Creates for given index the row in the actual worksheet.
	 * 
	 * @param rowIndex
	 * @return
	 */
	private XSSFRow createRowIfNotExists(int rowIndex) {
		XSSFRow row = xssfWorksheet.getRow(rowIndex);
		if (null == row)
			row = xssfWorksheet.createRow(rowIndex);
		return row;
	}
	
	/**
	 * creates the worksheet cell for given row and column, fills it with the given
	 * text and the given style. If style is null, the cell is styled default
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 *            - Text of cell
	 * @param style
	 *            - CellStyle - if null - default style.
	 */
	private void createCellIfNotExistsAndSet(int rowIndex, int columnIndex, Double text, XSSFCellStyle style) {
		XSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
		if (style != null)
			cell.setCellStyle(style);
	}

	/**
	 * creates the worksheet cell for given row and column, fills it with the given
	 * text and the given style. If style is null, the cell is styled default
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 *            - Text of cell
	 * @param style
	 *            - CellStyle - if null - default style.
	 */
	private void createCellIfNotExistsAndSet(int rowIndex, int columnIndex, String text, XSSFCellStyle style) {
		XSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
		if (style != null)
			cell.setCellStyle(style);

	}
	
	private void autoFitCells() {
		// fit the content to cell size
		xssfWorksheet.autoSizeColumn(0);
		for (int i = 1; i < 5; i++)
			xssfWorksheet.setColumnWidth(i, 12 * 256);
	}

	/**
	 * Writes the given {@link File} in a buffered {@link FileOutputStream}.
	 * 
	 * @param excelfile
	 * @throws Exception
	 */
	private void write(File excelfile) throws Exception {
		BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(excelfile));
		try {
			xssfWorkbook.write(outStream);
			outStream.flush();
		} finally {
			outStream.close();
		}
	}
}
