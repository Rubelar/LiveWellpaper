package com.rubelar35gmail.livewellpaper.Remote;

import com.rubelar35gmail.livewellpaper.Models.AnalyzeModel.ComputerVision;
import com.rubelar35gmail.livewellpaper.Models.AnalyzeModel.URLUpload;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface IComputerVision {
    @Headers({
            "Content_Type:application/json",
            "Ocp-Apim-Subscription-Key:fc48d00e6d804695aaa87816c8040330"

    })
    @POST
    Call<ComputerVision>analyzeImage(@Url String apiEndpoint, @Body URLUpload url);

}
