package com.redhat.gps.pathfinder.web.api;

import com.redhat.gps.pathfinder.domain.*;
import com.redhat.gps.pathfinder.repository.*;
import com.redhat.gps.pathfinder.web.api.model.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pathfinder")
public class CustomerAPIImpl implements CustomersApi {

    private final Logger log = LoggerFactory.getLogger(CustomerAPIImpl.class);

    private final CustomerRepository custRepo;

    private final ApplicationsRepository appsRepo;

    private final AssessmentsRepository assmRepo;

    private final QuestionMetaDataRepository questionRepository;

    private final ReviewsRepository reviewRepository;

    public CustomerAPIImpl(CustomerRepository custRepo, ApplicationsRepository appsRepo, AssessmentsRepository assmRepo, QuestionMetaDataRepository questionRepository, ReviewsRepository reviewRepository) {
        this.custRepo = custRepo;
        this.appsRepo = appsRepo;
        this.assmRepo = assmRepo;
        this.questionRepository = questionRepository;
        this.reviewRepository = reviewRepository;
    }

    public ResponseEntity<AssessmentType> customersCustIdApplicationsAppIdAssessmentsAssessIdGet(@ApiParam(value = "", required = true) @PathVariable("custId") String custId, @ApiParam(value = "", required = true) @PathVariable("appId") String appId, @ApiParam(value = "", required = true) @PathVariable("assessId") String assessId) {
        log.debug("customersCustIdApplicationsAppIdAssessmentsAssessIdGet....");

        AssessmentType resp = new AssessmentType();
        try {
            Assessments currAssm = assmRepo.findOne(assessId);
            if (currAssm != null) {
                resp.setDeps(currAssm.getDeps());
                AssessmentResponse tempPayload = new AssessmentResponse();

                Iterator it = currAssm.getResults().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    tempPayload.put((String) pair.getKey(), (String) pair.getValue());
                }
                resp.setPayload(tempPayload);
                return new ResponseEntity<AssessmentType>(resp, HttpStatus.OK);
            } else {
                return new ResponseEntity<AssessmentType>(resp, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            log.error("customersCustIdApplicationsAppIdAssessmentsAssessIdGet", ex.getMessage(), ex);
            return new ResponseEntity<AssessmentType>(resp, HttpStatus.BAD_REQUEST);
        }
    }


    public ResponseEntity<List<String>> customersCustIdApplicationsAppIdAssessmentsGet(@ApiParam(value = "", required = true) @PathVariable("custId") String custId, @ApiParam(value = "", required = true) @PathVariable("appId") String appId) {
        log.debug("customersCustIdApplicationsAppIdAssessmentsGet....");
        ArrayList<String> results = new ArrayList<>();
        try {
            Applications currApp = appsRepo.findOne(appId);
            try {

                if (currApp != null) {

                    List<Assessments> res = currApp.getAssessments();
                    if ((res != null) && (!res.isEmpty())) {
                        for (Assessments x : res) {
                            results.add(x.getId());
                        }
                    }
                    return new ResponseEntity<List<String>>(results, HttpStatus.OK);

                }
            } catch (Exception ex) {
                log.error("Unable to get assessments for customer ", ex.getMessage(), ex);
            }
            return new ResponseEntity<List<String>>(results, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("customersCustIdApplicationsAppIdAssessmentsGet", ex.getMessage(), ex);
        }
        return new ResponseEntity<List<String>>(results, HttpStatus.BAD_REQUEST);
    }


    public ResponseEntity<String> customersCustIdApplicationsAppIdAssessmentsPost
        (@ApiParam(value = "", required = true) @PathVariable("custId") String
             custId, @ApiParam(value = "", required = true) @PathVariable("appId") String
             appId, @ApiParam(value = "Application Assessment") @Valid @RequestBody AssessmentType body) {
        log.debug("customersCustIdApplicationsAppIdAssessmentsPost...." + body.getPayload());

        try {
            if (!custRepo.exists(custId)) {
                log.error("customersCustIdApplicationsAppIdAssessmentsPost....app not found " + appId);
                return new ResponseEntity<String>("Customer Not found", HttpStatus.BAD_REQUEST);
            }

            Applications currApp = appsRepo.findOne(appId);

            if (currApp != null) {

                Assessments newitem = new Assessments();
                newitem.setResults(body.getPayload());
                newitem.setDeps(body.getDeps());
                newitem.setDatetime(body.getDatetime());

                newitem = assmRepo.insert(newitem);

                List<Assessments> assmList = currApp.getAssessments();
                if (assmList == null) {
                    assmList = new ArrayList<>();
                }
                assmList.add(newitem);
                currApp.setAssessments(assmList);
                currApp = appsRepo.save(currApp);
                return new ResponseEntity<String>(newitem.getId(), HttpStatus.OK);
            } else {
                log.error("customersCustIdApplicationsAppIdAssessmentsPost....app not found " + appId);
                return new ResponseEntity<String>("Application not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            log.error("customersCustIdApplicationsAppIdAssessmentsPost Unable to create applications for customer ", ex.getMessage(), ex);
        }
        return new ResponseEntity<String>("Unable to create assessment", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<ApplicationType> customersCustIdApplicationsAppIdGet(String custId, String appId) {
        log.debug("customersCustIdApplicationsAppIdGet....");
        return null;
    }

    @Override
    public ResponseEntity<List<ApplicationType>> customersCustIdApplicationsGet
        (@ApiParam(value = "", required = true) @PathVariable("custId") String custId) {
        log.info("customersCustIdApplicationsGet....[" + custId + "]");
        ArrayList<ApplicationType> response = new ArrayList<>();

        try {
            List<Applications> resp = custRepo.findOne(custId).getApplications();
            if ((resp != null) && (!resp.isEmpty())) {
                for (Applications x : resp) {
                    ApplicationType lapp = new ApplicationType();
                    lapp.setName(x.getName());
                    lapp.setId(x.getId());
                    lapp.setReview(x.getReview().getId());
                    lapp.setDescription(x.getDescription());
                    response.add(lapp);
                }
            }
        } catch (Exception ex) {
            log.error("Unable to list applications for customer ", ex.getMessage(), ex);
            return new ResponseEntity<List<ApplicationType>>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<List<ApplicationType>>(response, HttpStatus.OK);
    }

    public ResponseEntity<String> customersCustIdApplicationsPost
        (@ApiParam(value = "Customer Identifier", required = true) @PathVariable("custId") String
             custId, @ApiParam(value = "Application Definition") @Valid @RequestBody ApplicationType body) {
        log.debug("customersCustIdApplicationsPost....");
        Customer myCust = custRepo.findOne(custId);
        if (myCust == null) {
            return new ResponseEntity<String>(custId, HttpStatus.BAD_REQUEST);
        } else {
            Applications app = new Applications();
            app.setName(body.getName());
            app.setDescription(body.getDescription());
            app = appsRepo.save(app);
            List<Applications> appList = myCust.getApplications();
            if (appList == null) {
                appList = new ArrayList<Applications>();
            }
            appList.add(app);
            myCust.setApplications(appList);
            custRepo.save(myCust);
            return new ResponseEntity<String>(app.getId(), HttpStatus.OK);
        }
    }

    public ResponseEntity<CustomerType> customersCustIdGet
        (@ApiParam(value = "Customer Identifier", required = true) @PathVariable("custId") String custId) {
        log.debug("customersCustIdGet....");
        Customer myCust = custRepo.findOne(custId);
        if (myCust == null) {
            return new ResponseEntity<CustomerType>(HttpStatus.BAD_REQUEST);
        } else {
            CustomerType resp = new CustomerType();
            resp.setCustomerName(myCust.getName());
            resp.setCustomerId(myCust.getId());
            resp.setCustomerDescription(myCust.getId());
            return new ResponseEntity<CustomerType>(resp, HttpStatus.OK);
        }
    }

    public ResponseEntity<String> customersPost(@ApiParam(value = "") @Valid @RequestBody CustomerType body) {
        log.debug("customersPost....");
        Customer myCust = new Customer();
        myCust.setName(body.getCustomerName());
        myCust.setSize(body.getCustomerVertical());
        myCust.setSize(body.getCustomerSize());
        try {
            myCust = custRepo.insert(myCust);
        } catch (Exception ex) {
            log.error("Unable to Create customer ", ex.getMessage(), ex);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<String>(myCust.getId(), HttpStatus.OK);
    }


    public ResponseEntity<List<CustomerType>> customersGet() {
        log.debug("customersGet....");
        ArrayList<CustomerType> response = new ArrayList<>();
        List<Customer> myCust = custRepo.findAll();
        if (myCust == null) {
            return new ResponseEntity<List<CustomerType>>(response, HttpStatus.BAD_REQUEST);
        } else {
            for (Customer x : myCust) {
                CustomerType resp = new CustomerType();
                resp.setCustomerName(x.getName());
                resp.setCustomerId(x.getId());
                resp.setCustomerSize(x.getSize());
                resp.setCustomerVertical(x.getVertical());
                response.add(resp);
            }
            return new ResponseEntity<List<CustomerType>>(response, HttpStatus.OK);
        }
    }


    public ResponseEntity<AssessmentProcessType> customersCustIdApplicationsAppIdAssessmentsAssessIdProcessGet
        (@ApiParam(value = "", required = true) @PathVariable("custId") String
             custId, @ApiParam(value = "", required = true) @PathVariable("appId") String
             appId, @ApiParam(value = "", required = true) @PathVariable("assessId") String assessId) {
        log.debug("customersCustIdApplicationsAppIdAssessmentsAssessIdProcessGet....");

        AssessmentProcessType resp = new AssessmentProcessType();
        List<AssessmentProcessQuestionResultsType> assessResults = new ArrayList<>();

        try {

            Assessments currAssm = assmRepo.findOne(assessId);
            if (currAssm == null) {
                return new ResponseEntity<AssessmentProcessType>(resp, HttpStatus.BAD_REQUEST);
            }

            List<QuestionMetaData> questionData = questionRepository.findAll();


            for (QuestionMetaData currQuestion : questionData) {
                String res = (String) currAssm.getResults().get(currQuestion.getId());
                AssessmentProcessQuestionResultsType vals = new AssessmentProcessQuestionResultsType();
                vals.setQuestionTag(currQuestion.getId());

                QuestionWeights.QuestionRank answerRank = currQuestion.getMetaData().get(
                    Integer.parseInt(res)
                ).getRank();
                vals.setQuestionRank(answerRank.ordinal());
                assessResults.add(vals);

                log.debug(currQuestion.getId() + ": value=" + res + " RANK " + answerRank.toString());

            }
            resp.setAssessResults(assessResults);
            resp.setAssmentNotes(currAssm.getResults().get("NOTES"));
            resp.setDependencies(currAssm.getDeps());
            resp.setBusinessPriority(currAssm.getResults().get("BUSPRIORITY"));

        } catch (Exception ex) {
            log.error("Error while processing assessment", ex.getMessage(), ex);
            return new ResponseEntity<AssessmentProcessType>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<AssessmentProcessType>(resp, HttpStatus.OK);
    }


    public ResponseEntity<String> customersCustIdApplicationsAppIdReviewPost(@ApiParam(value = "", required = true) @PathVariable("custId") String custId, @ApiParam(value = "", required = true) @PathVariable("appId") String appId, @ApiParam(value = "Application Definition") @Valid @RequestBody ReviewType body) {
        log.debug("customersCustIdApplicationsAppIdReviewPost....");
        try {
            Applications app = appsRepo.findOne(appId);
            if (app == null) {
                log.error("Error while processing review - Unable to find application with id ", appId);
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            }

            Assessments assm = assmRepo.findOne(body.getAssessmentId());
            if (assm == null) {
                log.error("Error while processing review - Unable to find assessment with id ", body.getAssessmentId());
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            }

            ApplicationAssessmentReview reviewData = new ApplicationAssessmentReview(
                body.getReviewTimestamp(),
                assm,
                body.getReviewDecision(),
                body.getWorkEffort(),
                body.getReviewNotes(),
                body.getWorkPriority(),
                body.getBusinessPriority(),
                app);

            if (app.getReview() != null)
                reviewData.setId(app.getReview().getId());

            reviewData = reviewRepository.save(reviewData);
            app.setReview(reviewData);
            appsRepo.save(app);

            return new ResponseEntity<String>(reviewData.getId(), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error while processing review", ex.getMessage(), ex);
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<ReviewType> customersCustIdApplicationsAppIdReviewReviewIdGet(@ApiParam(value = "", required = true) @PathVariable("custId") String custId, @ApiParam(value = "", required = true) @PathVariable("appId") String appId, @ApiParam(value = "", required = true) @PathVariable("reviewId") String reviewId) {
        log.debug("customersCustIdApplicationsAppIdReviewReviewIdGet....");
        ReviewType resp = new ReviewType();
        try {
            Applications app = appsRepo.findOne(appId);
            if (app == null) {
                log.error("Error while retrieving review - Unable to find application with id ", appId);
                return new ResponseEntity<ReviewType>(HttpStatus.BAD_REQUEST);
            }

            ApplicationAssessmentReview reviewData = reviewRepository.findOne(app.getReview().getId());

            if (reviewData == null) {
                log.error("Error while retrieving review - Unable to find review for application ", appId);
                return new ResponseEntity<ReviewType>(HttpStatus.BAD_REQUEST);
            }

            resp.setReviewDecision(reviewData.getReviewDecision());
            resp.setReviewNotes(reviewData.getReviewNotes());
            resp.setWorkEffort(reviewData.getReviewEstimate());
            resp.setReviewTimestamp(reviewData.getReviewDate());
            resp.setWorkPriority(reviewData.getWorkPriority());
            resp.setBusinessPriority(reviewData.getBusinessPriority());

            return new ResponseEntity<ReviewType>(resp, HttpStatus.OK);

        } catch (Exception ex) {
            log.error("Error while processing review", ex.getMessage(), ex);
            return new ResponseEntity<ReviewType>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
