/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.ground.persistence.local.room.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import com.google.android.ground.model.Survey;
import com.google.android.ground.model.job.Job;
import com.google.android.ground.model.mutation.SubmissionMutation;
import com.google.android.ground.persistence.local.LocalDataConsistencyException;
import com.google.android.ground.persistence.local.room.converter.ResponseDeltasConverter;
import com.google.android.ground.persistence.local.room.models.MutationEntitySyncStatus;
import com.google.android.ground.persistence.local.room.models.MutationEntityType;
import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;
import java.util.Date;
import org.json.JSONObject;

/** Representation of a {@link SubmissionMutation} in local data store. */
@AutoValue
@Entity(
    tableName = "submission_mutation",
    foreignKeys = {
      @ForeignKey(
          entity = LocationOfInterestEntity.class,
          parentColumns = "id",
          childColumns = "location_of_interest_id",
          onDelete = CASCADE),
      @ForeignKey(
          entity = SubmissionEntity.class,
          parentColumns = "id",
          childColumns = "submission_id",
          onDelete = CASCADE)
    },
    indices = {@Index("location_of_interest_id"), @Index("submission_id")})
public abstract class SubmissionMutationEntity extends MutationEntity {

  @CopyAnnotations
  @ColumnInfo(name = "location_of_interest_id")
  public abstract String getLocationOfInterestId();

  @CopyAnnotations
  @ColumnInfo(name = "job_id")
  public abstract String getJobId();

  @CopyAnnotations
  @ColumnInfo(name = "submission_id")
  public abstract String getSubmissionId();

  /**
   * For mutations of type {@link MutationEntityType#CREATE} and {@link MutationEntityType#UPDATE},
   * returns a {@link JSONObject} with the new values of modified task responses, with {@code null}
   * values representing responses that were removed/cleared.
   *
   * <p>This method returns {@code null} for mutation type {@link MutationEntityType#DELETE}.
   */
  @CopyAnnotations
  @Nullable
  @ColumnInfo(name = "response_deltas")
  public abstract String getResponseDeltas();

  public static SubmissionMutationEntity create(
      long id,
      String surveyId,
      String locationOfInterestId,
      String jobId,
      String submissionId,
      MutationEntityType type,
      MutationEntitySyncStatus syncStatus,
      String responseDeltas,
      long retryCount,
      @Nullable String lastError,
      @Nullable String userId,
      long clientTimestamp) {
    return builder()
        .setId(id)
        .setSurveyId(surveyId)
        .setLocationOfInterestId(locationOfInterestId)
        .setJobId(jobId)
        .setSubmissionId(submissionId)
        .setType(type)
        .setSyncStatus(syncStatus)
        .setResponseDeltas(responseDeltas)
        .setRetryCount(retryCount)
        .setLastError(lastError)
        .setUserId(userId)
        .setClientTimestamp(clientTimestamp)
        .build();
  }

  public static SubmissionMutationEntity fromMutation(SubmissionMutation m) {
    return SubmissionMutationEntity.builder()
        .setId(m.getId())
        .setSurveyId(m.getSurveyId())
        .setLocationOfInterestId(m.getLocationOfInterestId())
        .setJobId(m.getJob().getId())
        .setSubmissionId(m.getSubmissionId())
        .setType(MutationEntityType.fromMutationType(m.getType()))
        .setSyncStatus(MutationEntitySyncStatus.fromMutationSyncStatus(m.getSyncStatus()))
        .setResponseDeltas(ResponseDeltasConverter.toString(m.getResponseDeltas()))
        .setRetryCount(m.getRetryCount())
        .setLastError(m.getLastError())
        .setUserId(m.getUserId())
        .setClientTimestamp(m.getClientTimestamp().getTime())
        .build();
  }

  // Boilerplate generated using Android Studio AutoValue plugin:

  public static Builder builder() {
    return new AutoValue_SubmissionMutationEntity.Builder();
  }

  public SubmissionMutation toMutation(Survey survey) throws LocalDataConsistencyException {
    Job job =
        survey
            .getJob(getJobId())
            .orElseThrow(
                () ->
                    new LocalDataConsistencyException(
                        "Unknown jobId in submission mutation " + getId()));
    return SubmissionMutation.builder()
        .setSubmissionId(getSubmissionId())
        .setResponseDeltas(ResponseDeltasConverter.fromString(job, getResponseDeltas()))
        .setId(getId())
        .setSurveyId(getSurveyId())
        .setLocationOfInterestId(getLocationOfInterestId())
        .setJob(job)
        .setType(getType().toMutationType())
        .setSyncStatus(getSyncStatus().toMutationSyncStatus())
        .setRetryCount(getRetryCount())
        .setLastError(getLastError())
        .setUserId(getUserId())
        .setClientTimestamp(new Date(getClientTimestamp()))
        .build();
  }

  @AutoValue.Builder
  public abstract static class Builder extends MutationEntity.Builder<Builder> {

    public abstract Builder setLocationOfInterestId(String newLocationOfInterestId);

    public abstract Builder setJobId(String newJobId);

    public abstract Builder setSubmissionId(String newSubmissionId);

    public abstract Builder setResponseDeltas(String newResponseDeltas);

    public abstract SubmissionMutationEntity build();
  }
}
