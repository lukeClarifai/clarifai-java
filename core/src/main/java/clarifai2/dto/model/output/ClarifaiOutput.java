package clarifai2.dto.model.output;

import clarifai2.dto.prediction.FaceEmbedding;
import clarifai2.internal.grpc.api.ColorOuterClass;
import clarifai2.internal.grpc.api.ConceptOuterClass;
import clarifai2.internal.grpc.api.DataOuterClass;
import clarifai2.internal.grpc.api.EmbeddingOuterClass;
import clarifai2.internal.grpc.api.OutputOuterClass;
import clarifai2.api.BaseClarifaiClient;
import clarifai2.dto.ClarifaiStatus;
import clarifai2.dto.HasClarifaiIDRequired;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.ModelType;
import clarifai2.dto.prediction.Color;
import clarifai2.dto.prediction.Concept;
import clarifai2.dto.prediction.Embedding;
import clarifai2.dto.prediction.Frame;
import clarifai2.dto.prediction.Detection;
import clarifai2.dto.prediction.Prediction;
import clarifai2.exception.ClarifaiException;
import clarifai2.grpc.DateTimeConverter;
import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("NullableProblems")
@AutoValue
public abstract class ClarifaiOutput<PREDICTION extends Prediction> implements HasClarifaiIDRequired {

  ClarifaiOutput() {} // AutoValue instances only

  @NotNull public abstract Date createdAt();

  @NotNull public abstract Model<PREDICTION> model();

  @Nullable public abstract ClarifaiInput input();

  @NotNull public abstract List<PREDICTION> data();

  @NotNull public abstract ClarifaiStatus status();

  public static <PREDICTION extends Prediction> ClarifaiOutput<PREDICTION> deserialize(
      OutputOuterClass.Output output, BaseClarifaiClient client
  ) {
    ModelType modelType = ModelType.determineModelType(output);
    return (ClarifaiOutput<PREDICTION>) new AutoValue_ClarifaiOutput<>(
        output.getId(),
        DateTimeConverter.timestampToDate(output.getCreatedAt()),
        Model.deserialize(output.getModel(), client),
        output.getInput().hasData() ? ClarifaiInput.deserialize(output.getInput()) : null,
        deserializePredictions(modelType, output),
        ClarifaiStatus.deserialize(output.getStatus())
    );
  }

  private static <PREDICTION extends Prediction> List<PREDICTION> deserializePredictions(
      ModelType modelType, OutputOuterClass.Output output
  ) {
    DataOuterClass.Data data = output.getData();

    List<Prediction> predictions = new ArrayList<>();

    switch (modelType) {
      case COLOR:
      {
        for (ColorOuterClass.Color color : data.getColorsList()) {
          predictions.add(Color.deserialize(color));
        }
        break;
      }
      case CONCEPT:
      {
        for (ConceptOuterClass.Concept concept : data.getConceptsList()) {
          predictions.add(Concept.deserialize(concept));
        }
        break;
      }
      case EMBEDDING:
      {
        for (EmbeddingOuterClass.Embedding embedding : data.getEmbeddingsList()) {
          predictions.add(Embedding.deserialize(embedding));
        }
        break;
      }
      case FACE_EMBEDDING:
      {
        for (DataOuterClass.Region region : data.getRegionsList()) {
          predictions.add(FaceEmbedding.deserialize(region));
        }
        break;
      }
      case DETECT_CONCEPT:
      {
        for (DataOuterClass.Region detection : data.getRegionsList()) {
          predictions.add(Detection.deserialize(detection));
        }
        break;
      }
      case DETECTION:
      {
        for (DataOuterClass.Region detection : data.getRegionsList()) {
          predictions.add(Detection.deserialize(detection));
        }
        break;
      }
      case VIDEO:
      {
        for (DataOuterClass.Frame frame : data.getFramesList()) {
          predictions.add(Frame.deserialize(frame));
        }
        break;
      }
      default:
      {
        throw new ClarifaiException("Deserialization of " + modelType.toString() + " is not implemented");
      }
    }
    return (List<PREDICTION>) predictions;
  }
}
