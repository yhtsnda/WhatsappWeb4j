package it.auties.whatsapp.protobuf.info;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.protobuf.contact.ContactJid;
import it.auties.whatsapp.protobuf.product.ProductListHeaderImage;
import it.auties.whatsapp.protobuf.product.ProductSection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * A model class that holds the information related to a list of products.
 * This class is only a model, this means that changing its values will have no real effect on WhatsappWeb's servers.
 * Instead, methods inside {@link Whatsapp} should be used.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Accessors(fluent = true)
public final class ProductListInfo implements WhatsappInfo {
  /**
   * The products that this message wraps
   */
  @JsonProperty("1")
  @JsonPropertyDescription("ProductSection")
  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  private List<ProductSection> productSections;

  /**
   * The header image of the messages that this message wraps
   */
  @JsonProperty("2")
  @JsonPropertyDescription("ProductListHeaderImage")
  private ProductListHeaderImage headerImage;

  /**
   * The id of the seller of the products that this message wraps
   */
  @JsonProperty("3")
  @JsonPropertyDescription("string")
  private ContactJid sellerId;
}
