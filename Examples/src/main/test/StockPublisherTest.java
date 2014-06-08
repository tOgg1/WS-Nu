//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

import org.junit.Test;
import org.ntnunotif.wsnu.examples.StockBroker.StockPublisher;
import org.ntnunotif.wsnu.examples.StockBroker.generated.StockChanged;

import java.io.*;
import java.util.regex.Pattern;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 *
 */
public class StockPublisherTest {

    @Test
    public void testRegex(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("Examples/src/main/resources/WebPage2904.txt")));
            StringBuilder sb = new StringBuilder();
            String s;
            while((s = reader.readLine()) != null){
                sb.append(s);
            }

            s = sb.toString();

            String america = "All American Indices";
            String europe = "All European Indices";
            String asia = "Asia/Pacific Indices";
            String africa = "Africa/Middle East Indices";

            int indexOne = s.indexOf(america);
            int indexTwo = s.indexOf(europe);
            int indexThree = s.indexOf(asia);
            int indexFour = s.indexOf(africa);

            String americaData = s.substring(indexOne, indexTwo);
            String europeData = s.substring(indexTwo, indexThree);
            String asiaData = s.substring(indexThree, indexFour);
            String africadData = s.substring(indexFour);

            Pattern symbolPattern;
            Pattern namePattern;
            Pattern datePattern;
            Pattern valuePattern;
            Pattern absChangePattern;
            Pattern relChangePattern;

            // Jump over headers
            String row = americaData.substring(americaData.indexOf("</tbody>"));

            int tagStartIndex = row.indexOf("<tr");
            int tagEndIndex = row.indexOf("</tr>", tagStartIndex+1) + 5;

            row = row.substring(tagStartIndex, tagEndIndex);

            // Remove junk
            row = row.replaceAll("\n", "");
            row = row.replaceAll("\t", "");
            row = row.replaceAll("<td><a href=\"/do/latestArticleInChannel[?]channel=(.*)MktRpt\">View</a></td>", "");
            row = row.replaceAll("<td></td>", "");

            int tagStart = row.indexOf("<td>") + 4;
            int tagEnd = row.indexOf("</td>");

            String symbolCell = row.substring(tagStart, tagEnd);
            String symbolTemp = symbolCell.substring(symbolCell.indexOf("<a href=\"index?"));
            String symbol = symbolTemp.substring(symbolTemp.indexOf(">") + 1, symbolTemp.indexOf("</a>"));
            assertEquals(".DJI", symbol);

            row = row.substring(tagEnd+5);

            tagStart = row.indexOf("<td") + 4;
            tagEnd = row.indexOf("</td>");

            String nameCell = row.substring(tagStart, tagEnd);
            String name = nameCell;
            assertEquals("Dow Jones Industrial Average", name);

            row = row.substring(tagEnd+5);

            tagStart = row.indexOf("<td");
            tagEnd = row.indexOf("</td>");

            String dateCell = row.substring(tagStart, tagEnd);
            dateCell = dateCell.replaceAll("<!--(.*)-->", "");
            String date = dateCell.substring(dateCell.indexOf(">") +1);

            assertEquals("29 Apr 2014", date);

            row = row.substring(tagEnd+5);

            tagStart = row.indexOf("<td");
            tagEnd = row.indexOf("</td>");

            String valueCell = row.substring(tagStart, tagEnd);
            String value = valueCell.substring(valueCell.indexOf(">")+ 1);

            assertEquals("16,535.37", value);

            float valueParsed = Float.parseFloat(value.replaceAll(",", ""));

            assertEquals(16535.369140625, valueParsed, 0.01);

            row = row.substring(tagEnd+5);

            tagStart = row.indexOf("<td");
            tagEnd = row.indexOf("</td>");

            String changeAbsCell = row.substring(tagStart, tagEnd);
            String changeAbs = changeAbsCell.substring(changeAbsCell.indexOf(">") + 1);

            assertEquals("+86.63", changeAbs);

            float changeAbsParsed = Float.parseFloat(changeAbs.replaceAll("[+]?[-]?", ""));
            changeAbsParsed = changeAbs.charAt(0) == '+' ? changeAbsParsed : -changeAbsParsed;

            assertEquals(86.63, changeAbsParsed, 0.01);

            row = row.substring(tagEnd+5);

            tagStart = row.indexOf("<td");
            tagEnd = row.indexOf("</td>");

            String changeRelCell = row.substring(tagStart, tagEnd);
            String changeRel = changeRelCell.substring(changeRelCell.indexOf(">") + 1);

            assertEquals("+0.53%", changeRel);

            float changeRelParsed = Float.parseFloat(changeRel.replaceAll("[+]?[-]?", "").replaceAll("%", ""));
            changeRelParsed = changeRel.charAt(0) == '+' ? changeRelParsed : -changeRelParsed;

            assertEquals(0.53, changeRelParsed, 0.01);

            StockChanged stockChanged = new StockChanged();
            stockChanged.setSymbol(symbol);
            stockChanged.setName(name);
            stockChanged.setLastChange(date);
            stockChanged.setValue(valueParsed);
            stockChanged.setChangeAbsolute(changeAbsParsed);
            stockChanged.setChangeRelative(changeRelParsed);

            System.out.println(stockChanged.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateAll(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("Examples/src/main/resources/WebPage2904.txt")));
            StringBuilder sb = new StringBuilder();
            String s;

            while((s = reader.readLine()) != null){
                sb.append(s);
            }

            s = sb.toString();

            String america = "All American Indices";
            String europe = "All European Indices";
            String asia = "Asia/Pacific Indices";
            String africa = "Africa/Middle East Indices";

            int indexOne = s.indexOf(america);
            int indexTwo = s.indexOf(europe);
            int indexThree = s.indexOf(asia);
            int indexFour = s.indexOf(africa);

            String americaData = s.substring(indexOne, indexTwo);
            String europeData = s.substring(indexTwo, indexThree);
            String asiaData = s.substring(indexThree, indexFour);
            String africaData = s.substring(indexFour);

            // Jump over headers
            americaData = americaData.substring(americaData.indexOf("</tbody>"));
            europeData = europeData.substring(americaData.indexOf("</tbody>"));
            asiaData = asiaData.substring(americaData.indexOf("</tbody>"));
            africaData = africaData.substring(americaData.indexOf("</tbody>"));

            int tagStartIndex;
            int tagEndIndex;

            String[] dataSets = new String[]{americaData, europeData, asiaData, africaData};

            for (int i = 0; i < dataSets.length; i++) {
                String dataSet = dataSets[i];

                while((tagStartIndex = dataSet.indexOf("<tr")) > 0) {
                    tagEndIndex = dataSet.indexOf("</tr>", tagStartIndex + 1) + 5;

                    String row = dataSet.substring(tagStartIndex, tagEndIndex);

                    // Remove junk
                    row = row.replaceAll("\n", "");
                    row = row.replaceAll("\t", "");
                    row = row.replaceAll("<td><a href=\"/do/latestArticleInChannel[?]channel=(.*)MktRpt\">View</a></td>", "");
                    row = row.replaceAll("<td></td>", "");

                    if(row.split("<td").length != 7){
                        dataSet = dataSet.substring(tagEndIndex);
                        continue;
                    }

                    int tagStart = row.indexOf("<td>") + 4;
                    int tagEnd = row.indexOf("</td>");

                    String symbolCell = row.substring(tagStart, tagEnd);
                    String symbolTemp = symbolCell.substring(symbolCell.indexOf("<a href=\"index?"));
                    String symbol = symbolTemp.substring(symbolTemp.indexOf(">") + 1, symbolTemp.indexOf("</a>"));

                    row = row.substring(tagEnd + 5);

                    tagStart = row.indexOf("<td") + 4;
                    tagEnd = row.indexOf("</td>");

                    String nameCell = row.substring(tagStart, tagEnd);
                    String name = nameCell;

                    row = row.substring(tagEnd + 5);

                    tagStart = row.indexOf("<td");
                    tagEnd = row.indexOf("</td>");

                    String dateCell = row.substring(tagStart, tagEnd);
                    dateCell = dateCell.replaceAll("<!--(.*)-->", "");
                    String date = dateCell.substring(dateCell.indexOf(">") + 1);

                    row = row.substring(tagEnd + 5);

                    tagStart = row.indexOf("<td");
                    tagEnd = row.indexOf("</td>");

                    String valueCell = row.substring(tagStart, tagEnd);
                    String value = valueCell.substring(valueCell.indexOf(">") + 1);

                    float valueParsed = Float.parseFloat(value.replaceAll(",", ""));

                    row = row.substring(tagEnd + 5);

                    tagStart = row.indexOf("<td");
                    tagEnd = row.indexOf("</td>");

                    String changeAbsCell = row.substring(tagStart, tagEnd);
                    String changeAbs = changeAbsCell.substring(changeAbsCell.indexOf(">") + 1);

                    float changeAbsParsed = Float.parseFloat(changeAbs.replaceAll("[+]?[-]?[,]?", ""));
                    changeAbsParsed = changeAbs.charAt(0) == '+' ? changeAbsParsed : -changeAbsParsed;

                    row = row.substring(tagEnd + 5);

                    tagStart = row.indexOf("<td");
                    tagEnd = row.indexOf("</td>");

                    String changeRelCell = row.substring(tagStart, tagEnd);
                    String changeRel = changeRelCell.substring(changeRelCell.indexOf(">") + 1);

                    float changeRelParsed = Float.parseFloat(changeRel.replaceAll("[+]?[-]?", "").replaceAll("%", ""));
                    changeRelParsed = changeRel.charAt(0) == '+' ? changeRelParsed : -changeRelParsed;

                    StockChanged stockChanged = new StockChanged();
                    stockChanged.setSymbol(symbol);
                    stockChanged.setName(name);
                    stockChanged.setLastChange(date);
                    stockChanged.setValue(valueParsed);
                    stockChanged.setChangeAbsolute(changeAbsParsed);
                    stockChanged.setChangeRelative(changeRelParsed);

                    System.out.println(stockChanged.toString());

                    dataSet = dataSet.substring(tagEndIndex);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAssertFloat(){
        float one = 1.00f;
        float two = 1.04f;
        float three = 10.3f/10.0f;
        float four = 1.0f/3.0f;
        float five = 0.333333333f;

        assertTrue(StockPublisher.assertFloat(four, five));
        assertFalse(StockPublisher.assertFloat(one, two));
        assertTrue(StockPublisher.assertFloat(four * five / five, four));
        assertTrue(StockPublisher.assertFloat(one * 5, 5.0f));
    }

    @Test
    public void testStockEquals(){

    }
}
