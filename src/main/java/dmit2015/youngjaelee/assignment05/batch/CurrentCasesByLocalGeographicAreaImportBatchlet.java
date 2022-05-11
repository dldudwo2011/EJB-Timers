/**
 * @author Youngjae Lee
 * @version 2022-03-07
 *
 * description: Property assessment processor
 */

package dmit2015.youngjaelee.assignment05.batch;

import dmit2015.youngjaelee.assignment05.entity.CurrentCasesByLocalGeographicArea;
import dmit2015.youngjaelee.assignment05.repository.CurrentCasesByLocalGeographicAreaRepository;
import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.runtime.BatchStatus;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.WKTReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Named
public class CurrentCasesByLocalGeographicAreaImportBatchlet extends AbstractBatchlet {

    @Inject
    private CurrentCasesByLocalGeographicAreaRepository _currentCasesRepository;

    @Inject
    @ConfigProperty(name="dmit2015.youngjaelee.DownloadFolder")
    private String csvFilePath;


    @Override
    public String process() throws Exception {
        _currentCasesRepository.deleteAll();

        try (BufferedReader reader = new BufferedReader(new FileReader(Paths.get(csvFilePath).toFile()))){
            reader.readLine();

            String line = null;

            while ((line = reader.readLine()) != null)
            {
                try{
                    final String delimiter = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
                    String[] tokens = line.split(delimiter, -1);

//            tokens[5].trim().equals("") ? null : Integer.parseInt(tokens[5])

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

                    CurrentCasesByLocalGeographicArea currentCasesByLocalGeographicArea = new CurrentCasesByLocalGeographicArea();
                    currentCasesByLocalGeographicArea.setDate(LocalDate.parse(tokens[0], formatter));
                    currentCasesByLocalGeographicArea.setLocation(tokens[1]);
                    currentCasesByLocalGeographicArea.setVaccinationDataPopulation(tokens[2].trim().equals("") ? null : Integer.parseInt(tokens[2]));
                    currentCasesByLocalGeographicArea.setCaseDataPopulation(tokens[3].trim().equals("") ? null : Integer.parseInt(tokens[3]));
                    currentCasesByLocalGeographicArea.setTotalCases(tokens[4].trim().equals("") ? null : Integer.parseInt(tokens[4]));
                    currentCasesByLocalGeographicArea.setActiveCases(tokens[5].trim().equals("") ? null : Integer.parseInt(tokens[5]));
                    currentCasesByLocalGeographicArea.setActiveCaseRate(tokens[6].trim().equals("") ? null : Double.parseDouble(tokens[6]));
                    currentCasesByLocalGeographicArea.setRecoveredCases(tokens[7].trim().equals("") ? null : Integer.parseInt(tokens[7]));
                    currentCasesByLocalGeographicArea.setDeaths(tokens[8].trim().equals("") ? null : Integer.parseInt(tokens[8]));
                    currentCasesByLocalGeographicArea.setOneDosedPopulation(tokens[9].trim().equals("") ? null : Integer.parseInt(tokens[9]));
                    currentCasesByLocalGeographicArea.setFullyDosedPopulation(tokens[10].trim().equals("") ? null : Integer.parseInt(tokens[10]));
                    currentCasesByLocalGeographicArea.setTotalDosesAdministered(tokens[11].trim().equals("") ? null : Integer.parseInt(tokens[11]));
                    currentCasesByLocalGeographicArea.setPercentAtLeastOneDose(tokens[12].trim().equals("") ? null : Double.parseDouble(tokens[12]));
                    currentCasesByLocalGeographicArea.setPercentFullyImmunized(tokens[13].trim().equals("") ? null : Double.parseDouble(tokens[13]));

                    String wktText = tokens[14].replaceAll("\"", "");
                    MultiPolygon multiPolygon = (MultiPolygon) new WKTReader().read(wktText);
                    currentCasesByLocalGeographicArea.setPolygon(multiPolygon);

                    _currentCasesRepository.create(currentCasesByLocalGeographicArea);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
        return BatchStatus.COMPLETED.toString();
    }

}
