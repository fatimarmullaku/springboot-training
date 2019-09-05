package com.ucx.training.shop.service;

import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import com.ucx.training.shop.entity.Customer;
import com.ucx.training.shop.repository.PaymentInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripePayment implements PaymentInterface {

    @Autowired
    private CustomerService customerService;

    /**
     * Stripe Secret Key is used to communicate with Stripe API.
     * This is the TEST Stripe Secret Key and it's a must to initialize.
     */
    private static final String TEST_STRIPE_SECRET_KEY = "sk_test_F80HwLcBteuczUKFSiC2KEKw00TvPxtOTe";
    Map<String,String> token_id = new HashMap<>();

    public StripePayment(){
        Stripe.apiKey = TEST_STRIPE_SECRET_KEY;
    }

    /**
     * @param customerId -> Is the customer Id from which customer will be found on our DB.
     * @return -> This method will create a Stripe Customer based on Customer information provided from our DB,
     * with a test card as well (Test Card will be Visa Card)
     */
    public Map<String,String> createCustomer(int customerId){

        Customer foundCustomer = customerService.findById(customerId);
        Map<String,Object> customerParams = new HashMap<>();
        customerParams.put("description", foundCustomer.getName());
        customerParams.put("name", foundCustomer.getName());
        customerParams.put("email",foundCustomer.getUser().getEmail());

        Map<String,Object> cardParams = new HashMap<>();
        cardParams.put("source","tok_visa");

        try{
            com.stripe.model.Customer stripeCustomer = com.stripe.model.Customer.create(customerParams);
            token_id.put("customer_id",stripeCustomer.getId());
            com.stripe.model.Customer foundStripeCustomer = com.stripe.model.Customer.retrieve(token_id.get("customer_id"));
            foundStripeCustomer.getSources().create(cardParams);
            System.out.println(stripeCustomer);
        }catch(CardException e){
            e.printStackTrace();
        }catch(RateLimitException e){
            e.printStackTrace();
        }catch (InvalidRequestException e){
            e.printStackTrace();
        }catch (AuthenticationException e){
            e.printStackTrace();
        }catch (StripeException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

        return token_id;
    }

    /**
     * @return This will return the Stripe Customer Token Id from Stripe.
     */
    public String getTokenId(){
        return this.token_id.get("customer_id");
    }

    /**
     * This method will create a charge on Stripe with a specific amount.
     * Amount is on cents/pennies so we should multiply the amount with 100.
     * For creating a Stripe Charge the chargeParams should have a customer Token Id, otherwise the charge won't be created.
     */
    public void chargeCreditCard(BigDecimal amount){

        Map<String,Object> chargeParams = new HashMap<>();
        chargeParams.put("amount",amount.multiply(new BigDecimal("100")).intValueExact());
        chargeParams.put("currency","usd");
        chargeParams.put("description","One time charge");
        chargeParams.put("customer", this.getTokenId());

        try{
            Charge charge = Charge.create(chargeParams);
            System.out.println(charge.getPaymentMethod());
        }catch (CardException e){
            e.printStackTrace();
        }catch (RateLimitException e){
            e.printStackTrace();
        }catch (InvalidRequestException e){
            e.printStackTrace();
        }catch (AuthenticationException e){
            e.printStackTrace();
        }catch (ApiConnectionException e){
            e.printStackTrace();
        }catch (StripeException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}