import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.writable.Writable;
import org.datavec.spark.transform.SparkTransformExecutor;
import org.datavec.spark.transform.misc.StringToWritablesFunction;
import org.datavec.spark.transform.misc.WritablesToStringFunction;

import java.util.Date;
import java.util.List;

public class StormReportsRecordReader {
    public static void main(String[] args)throws Exception {
        int numLinesToSkip = 0;
        String delimiter = ",";

        String baseDir = "/Users/subhayuroy/weather/storm_reports/";
        String fileName = "reports.csv";
        String inputPath = baseDir + fileName;
        String timeStamp = String.valueOf(new Date().getTime());
        String outputPath = baseDir + "reports_processed_" + timeStamp;

        Schema inputDataSchema = new Schema.Builder()
                .addColumnsString("datetime","severity","location","county","state")
                .addColumnsDouble("lat","lon")
                .addColumnsString("comment")
                .addColumnCategorical("type","TOR","WIND","HAIL")
                .build();

        TransformProcess tp = new TransformProcess.Builder(inputDataSchema)
                .removeColumns("datetime","severity","location","county","state","comment")
                .categoricalToInteger("type")
                .build();

        int numActions = tp.getActionList().size();
        for (int i = 0; i<numActions; i++){
            System.out.println("\n\n===============================");
            System.out.println("--- Schema after step " + i +
            " (" + tp.getActionList().get(i) + ")--" );
            System.out.println(tp.getSchemaAfterStep(i));
        }

        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local[*]");
        sparkConf.setAppName("Storm Reports Record Reader Transform");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        // read the data file
        JavaRDD<String> lines = sc.textFile(inputPath);
        // convert to Writable
        JavaRDD<List<Writable>> stormReports = lines.map(new StringToWritablesFunction(new CSVRecordReader()));
        // run our transform process
        JavaRDD<List<Writable>> processed = SparkTransformExecutor.execute(stormReports,tp);
        // convert Writable back to string for export
        JavaRDD<String> toSave= processed.map(new WritablesToStringFunction(","));

        toSave.saveAsTextFile(outputPath);   
    }
}
