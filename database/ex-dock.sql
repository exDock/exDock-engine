--
-- PostgreSQL database dump
--

-- Dumped from database version 14.17
-- Dumped by pg_dump version 16.1

-- Started on 2025-04-07 14:57:48

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO postgres;

--
-- TOC entry 891 (class 1247 OID 16386)
-- Name: b_permissions; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.b_permissions AS ENUM (
  'none',
  'read',
  'read-write',
  'write'
  );


ALTER TYPE public.b_permissions OWNER TO postgres;

--
-- TOC entry 894 (class 1247 OID 16396)
-- Name: cpa_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.cpa_type AS ENUM (
  'bool',
  'float',
  'int',
  'money',
  'string'
  );


ALTER TYPE public.cpa_type OWNER TO postgres;

--
-- TOC entry 897 (class 1247 OID 16408)
-- Name: index; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.index AS ENUM (
  'index, follow',
  'index, nofollow',
  'noindex, follow',
  'noindex nofollow'
  );


ALTER TYPE public.index OWNER TO postgres;

--
-- TOC entry 900 (class 1247 OID 16418)
-- Name: p_index; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.p_index AS ENUM (
  'index, follow',
  'index, nofollow',
  'noindex, follow',
  'noindex nofollow'
  );


ALTER TYPE public.p_index OWNER TO postgres;

--
-- TOC entry 903 (class 1247 OID 16428)
-- Name: p_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.p_type AS ENUM (
  'product',
  'category',
  'text_page'
  );


ALTER TYPE public.p_type OWNER TO postgres;

--
-- TOC entry 278 (class 1255 OID 16435)
-- Name: check_root_url(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.check_root_url() RETURNS trigger
  LANGUAGE plpgsql
AS $$BEGIN
  IF (SELECT COUNT(*) FROM url_keys WHERE (url_key = '/' AND upper_key != '/') OR (url_key != '/' AND upper_key='/')) > 0
  THEN RAISE EXCEPTION 'The root is only allowed to be combined with the root!';
  END IF;
  RETURN NEW;
END;$$;


ALTER FUNCTION public.check_root_url() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 209 (class 1259 OID 16436)
-- Name: attribute_block; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.attribute_block (
                                      block_id integer NOT NULL,
                                      attribute_id character varying(255) NOT NULL
);


ALTER TABLE public.attribute_block OWNER TO postgres;

--
-- TOC entry 210 (class 1259 OID 16439)
-- Name: attribute_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.attribute_list (
                                     attribute_id character varying(255) NOT NULL,
                                     list_name character varying(255) NOT NULL
);


ALTER TABLE public.attribute_list OWNER TO postgres;

--
-- TOC entry 211 (class 1259 OID 16444)
-- Name: backend_block; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.backend_block (
                                    block_id integer NOT NULL,
                                    block_name character varying(255),
                                    block_type character varying(255)
);


ALTER TABLE public.backend_block OWNER TO postgres;

--
-- TOC entry 212 (class 1259 OID 16449)
-- Name: backend_block_block_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.backend_block_block_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  MAXVALUE 2147483647
  CACHE 1;


ALTER SEQUENCE public.backend_block_block_id_seq OWNER TO postgres;

--
-- TOC entry 3963 (class 0 OID 0)
-- Dependencies: 212
-- Name: backend_block_block_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.backend_block_block_id_seq OWNED BY public.backend_block.block_id;


--
-- TOC entry 213 (class 1259 OID 16450)
-- Name: backend_permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.backend_permissions (
                                          user_id integer NOT NULL,
                                          user_permissions public.b_permissions NOT NULL,
                                          server_settings public.b_permissions NOT NULL,
                                          template public.b_permissions NOT NULL,
                                          category_content public.b_permissions NOT NULL,
                                          category_products public.b_permissions NOT NULL,
                                          product_content public.b_permissions NOT NULL,
                                          product_price public.b_permissions NOT NULL,
                                          product_warehouse public.b_permissions NOT NULL,
                                          text_pages public.b_permissions NOT NULL,
                                          "API_KEY" character varying(128)
);


ALTER TABLE public.backend_permissions OWNER TO postgres;

--
-- TOC entry 214 (class 1259 OID 16453)
-- Name: block_attributes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.block_attributes (
                                       attribute_id character varying(100) NOT NULL,
                                       attribute_name character varying(255),
                                       attribute_type character varying(100)
);


ALTER TABLE public.block_attributes OWNER TO postgres;

--
-- TOC entry 215 (class 1259 OID 16456)
-- Name: block_id; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.block_id (
                               block_id integer NOT NULL,
                               category_id integer NOT NULL,
                               product_id integer NOT NULL
);


ALTER TABLE public.block_id OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 16459)
-- Name: blocks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.blocks (
                             template_key character varying(100) NOT NULL
);


ALTER TABLE public.blocks OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16462)
-- Name: categories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categories (
                                 category_id integer NOT NULL,
                                 upper_category integer,
                                 name character varying(100) NOT NULL,
                                 short_description text NOT NULL,
                                 description text NOT NULL
);


ALTER TABLE public.categories OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 16467)
-- Name: categories_category_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.categories_category_id_seq
  AS integer
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;


ALTER SEQUENCE public.categories_category_id_seq OWNER TO postgres;

--
-- TOC entry 3964 (class 0 OID 0)
-- Dependencies: 218
-- Name: categories_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.categories_category_id_seq OWNED BY public.categories.category_id;


--
-- TOC entry 219 (class 1259 OID 16468)
-- Name: categories_products; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categories_products (
                                          category_id integer NOT NULL,
                                          product_id integer NOT NULL
);


ALTER TABLE public.categories_products OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 16471)
-- Name: categories_seo; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categories_seo (
                                     category_id integer NOT NULL,
                                     meta_title text,
                                     meta_description text,
                                     meta_keywords text,
                                     page_index public.p_index NOT NULL
);


ALTER TABLE public.categories_seo OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16476)
-- Name: category_urls; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.category_urls (
                                    url_key character varying(100) NOT NULL,
                                    upper_key character varying(100) NOT NULL,
                                    category_id integer NOT NULL
);


ALTER TABLE public.category_urls OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 16479)
-- Name: custom_product_attributes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.custom_product_attributes (
                                                attribute_key character varying(64) NOT NULL,
                                                scope integer NOT NULL,
                                                name character varying(64) NOT NULL,
                                                type public.cpa_type NOT NULL,
                                                multiselect bit(1) NOT NULL,
                                                required bit(1) NOT NULL
);


ALTER TABLE public.custom_product_attributes OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16482)
-- Name: eav; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav (
                          product_id integer NOT NULL,
                          attribute_key character varying(64) NOT NULL
);


ALTER TABLE public.eav OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 16485)
-- Name: eav_attribute_bool; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_bool (
                                         attribute_id character varying(255) NOT NULL,
                                         attribute_key character varying(255) NOT NULL,
                                         value boolean
);


ALTER TABLE public.eav_attribute_bool OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16490)
-- Name: eav_attribute_float; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_float (
                                          attribute_id character varying(255) NOT NULL,
                                          attribute_key character varying(255) NOT NULL,
                                          value double precision
);


ALTER TABLE public.eav_attribute_float OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 16495)
-- Name: eav_attribute_int; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_int (
                                        attribute_id character varying(255) NOT NULL,
                                        attribute_key character varying(255) NOT NULL,
                                        value integer
);


ALTER TABLE public.eav_attribute_int OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 16500)
-- Name: eav_attribute_money; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_money (
                                          attribute_id character varying(255) NOT NULL,
                                          attribute_key character varying(255) NOT NULL,
                                          value numeric(11,2)
);


ALTER TABLE public.eav_attribute_money OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 16505)
-- Name: eav_attribute_multi_select; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_multi_select (
                                                 attribute_id character varying(255) NOT NULL,
                                                 attribute_key character varying(255) NOT NULL,
                                                 value integer
);


ALTER TABLE public.eav_attribute_multi_select OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 16510)
-- Name: eav_attribute_string; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_string (
                                           attribute_id character varying(255) NOT NULL,
                                           attribute_key character varying(255) NOT NULL,
                                           value character varying(255)
);


ALTER TABLE public.eav_attribute_string OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 16515)
-- Name: eav_global_bool; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_bool (
                                      product_id integer NOT NULL,
                                      attribute_key character varying(64) NOT NULL,
                                      value bit(1) NOT NULL
);


ALTER TABLE public.eav_global_bool OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 16518)
-- Name: eav_global_float; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_float (
                                       product_id integer NOT NULL,
                                       attribute_key character varying(64) NOT NULL,
                                       value double precision NOT NULL
);


ALTER TABLE public.eav_global_float OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 16521)
-- Name: eav_global_int; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_int (
                                     product_id integer NOT NULL,
                                     attribute_key character varying(64) NOT NULL,
                                     value integer NOT NULL
);


ALTER TABLE public.eav_global_int OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 16524)
-- Name: eav_global_money; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_money (
                                       product_id integer NOT NULL,
                                       attribute_key character varying(64) NOT NULL,
                                       value numeric(11,2) NOT NULL
);


ALTER TABLE public.eav_global_money OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 16527)
-- Name: eav_global_multi_select; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_multi_select (
                                              product_id integer NOT NULL,
                                              attribute_key character varying(64) NOT NULL,
                                              value integer NOT NULL
);


ALTER TABLE public.eav_global_multi_select OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 16530)
-- Name: eav_global_string; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_string (
                                        product_id integer NOT NULL,
                                        attribute_key character varying(64) NOT NULL,
                                        value text NOT NULL
);


ALTER TABLE public.eav_global_string OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 16535)
-- Name: eav_store_view_bool; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_store_view_bool (
                                          product_id integer NOT NULL,
                                          store_view_id integer NOT NULL,
                                          attribute_key character varying(64) NOT NULL,
                                          value bit(1) NOT NULL
);


ALTER TABLE public.eav_store_view_bool OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 16538)
-- Name: eav_store_view_float; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_store_view_float (
                                           product_id integer NOT NULL,
                                           store_view_id integer NOT NULL,
                                           attribute_key character varying(64) NOT NULL,
                                           value double precision NOT NULL
);


ALTER TABLE public.eav_store_view_float OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 16541)
-- Name: eav_store_view_int; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_store_view_int (
                                         product_id integer NOT NULL,
                                         store_view_id integer NOT NULL,
                                         attribute_key character varying(64) NOT NULL,
                                         value integer NOT NULL
);


ALTER TABLE public.eav_store_view_int OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 16544)
-- Name: eav_store_view_money; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_store_view_money (
                                           product_id integer NOT NULL,
                                           store_view_id integer NOT NULL,
                                           attribute_key character varying(64) NOT NULL,
                                           value numeric(11,2) NOT NULL
);


ALTER TABLE public.eav_store_view_money OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 16547)
-- Name: eav_store_view_multi_select; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_store_view_multi_select (
                                                  product_id integer NOT NULL,
                                                  store_view_id integer NOT NULL,
                                                  attribute_key character varying(64) NOT NULL,
                                                  value integer NOT NULL
);


ALTER TABLE public.eav_store_view_multi_select OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 16550)
-- Name: eav_store_view_string; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_store_view_string (
                                            product_id integer NOT NULL,
                                            store_view_id integer NOT NULL,
                                            attribute_key character varying(64) NOT NULL,
                                            value text NOT NULL
);


ALTER TABLE public.eav_store_view_string OWNER TO postgres;

--
-- TOC entry 242 (class 1259 OID 16555)
-- Name: eav_website_bool; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_website_bool (
                                       product_id integer NOT NULL,
                                       website_id integer NOT NULL,
                                       attribute_key character varying(64) NOT NULL,
                                       value bit(1) NOT NULL
);


ALTER TABLE public.eav_website_bool OWNER TO postgres;

--
-- TOC entry 243 (class 1259 OID 16558)
-- Name: eav_website_float; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_website_float (
                                        product_id integer NOT NULL,
                                        website_id integer NOT NULL,
                                        attribute_key character varying(64) NOT NULL,
                                        value double precision NOT NULL
);


ALTER TABLE public.eav_website_float OWNER TO postgres;

--
-- TOC entry 244 (class 1259 OID 16561)
-- Name: eav_website_int; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_website_int (
                                      product_id integer NOT NULL,
                                      website_id integer NOT NULL,
                                      attribute_key character varying(64) NOT NULL,
                                      value integer NOT NULL
);


ALTER TABLE public.eav_website_int OWNER TO postgres;

--
-- TOC entry 245 (class 1259 OID 16564)
-- Name: eav_website_money; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_website_money (
                                        product_id integer NOT NULL,
                                        website_id integer NOT NULL,
                                        attribute_key character varying(64) NOT NULL,
                                        value numeric(11,2) NOT NULL
);


ALTER TABLE public.eav_website_money OWNER TO postgres;

--
-- TOC entry 246 (class 1259 OID 16567)
-- Name: eav_website_multi_select; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_website_multi_select (
                                               product_id integer NOT NULL,
                                               website_id integer NOT NULL,
                                               attribute_key character varying(64) NOT NULL,
                                               value integer NOT NULL
);


ALTER TABLE public.eav_website_multi_select OWNER TO postgres;

--
-- TOC entry 247 (class 1259 OID 16570)
-- Name: eav_website_string; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_website_string (
                                         product_id integer NOT NULL,
                                         website_id integer NOT NULL,
                                         attribute_key character varying(64) NOT NULL,
                                         value text NOT NULL
);


ALTER TABLE public.eav_website_string OWNER TO postgres;

--
-- TOC entry 248 (class 1259 OID 16575)
-- Name: image; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.image (
                            image_url character varying(255) NOT NULL,
                            image_name character varying(255) NOT NULL,
                            extensions character varying(255) NOT NULL
);


ALTER TABLE public.image OWNER TO postgres;

--
-- TOC entry 249 (class 1259 OID 16580)
-- Name: image_product; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.image_product (
                                    product_id integer NOT NULL,
                                    image_url character varying(255) NOT NULL,
                                    image_name character varying(255) NOT NULL
);


ALTER TABLE public.image_product OWNER TO postgres;

--
-- TOC entry 250 (class 1259 OID 16585)
-- Name: list_items; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.list_items (
                                 list_item_id integer NOT NULL,
                                 item_key character varying(255) NOT NULL,
                                 item_position integer DEFAULT 0 NOT NULL,
                                 boolean_value boolean,
                                 float_value double precision,
                                 integer_value integer,
                                 money_value numeric(11,2),
                                 multi_select_value integer,
                                 string_value character varying(255)
);


ALTER TABLE public.list_items OWNER TO postgres;

--
-- TOC entry 251 (class 1259 OID 16591)
-- Name: list_item_list_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.list_item_list_item_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  MAXVALUE 2147483647
  CACHE 1;


ALTER SEQUENCE public.list_item_list_item_id_seq OWNER TO postgres;

--
-- TOC entry 3965 (class 0 OID 0)
-- Dependencies: 251
-- Name: list_item_list_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.list_item_list_item_id_seq OWNED BY public.list_items.list_item_id;


--
-- TOC entry 252 (class 1259 OID 16592)
-- Name: multi_select_attributes_bool; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.multi_select_attributes_bool (
                                                   attribute_key character varying(64) NOT NULL,
                                                   option integer NOT NULL,
                                                   value bit(1) NOT NULL
);


ALTER TABLE public.multi_select_attributes_bool OWNER TO postgres;

--
-- TOC entry 253 (class 1259 OID 16595)
-- Name: multi_select_attributes_float; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.multi_select_attributes_float (
                                                    attribute_key character varying(64) NOT NULL,
                                                    option integer NOT NULL,
                                                    value double precision NOT NULL
);


ALTER TABLE public.multi_select_attributes_float OWNER TO postgres;

--
-- TOC entry 254 (class 1259 OID 16598)
-- Name: multi_select_attributes_int; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.multi_select_attributes_int (
                                                  attribute_key character varying(64) NOT NULL,
                                                  option integer NOT NULL,
                                                  value integer NOT NULL
);


ALTER TABLE public.multi_select_attributes_int OWNER TO postgres;

--
-- TOC entry 255 (class 1259 OID 16601)
-- Name: multi_select_attributes_money; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.multi_select_attributes_money (
                                                    attribute_key character varying(64) NOT NULL,
                                                    option integer NOT NULL,
                                                    value numeric(11,2) NOT NULL
);


ALTER TABLE public.multi_select_attributes_money OWNER TO postgres;

--
-- TOC entry 256 (class 1259 OID 16604)
-- Name: multi_select_attributes_string; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.multi_select_attributes_string (
                                                     attribute_key character varying(64) NOT NULL,
                                                     option integer NOT NULL,
                                                     value text NOT NULL
);


ALTER TABLE public.multi_select_attributes_string OWNER TO postgres;

--
-- TOC entry 257 (class 1259 OID 16609)
-- Name: page_block; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.page_block (
                                 page_name character varying(255) NOT NULL,
                                 block_id integer NOT NULL
);


ALTER TABLE public.page_block OWNER TO postgres;

--
-- TOC entry 258 (class 1259 OID 16612)
-- Name: product_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.product_list (
                                   product_id integer NOT NULL,
                                   list_item_id integer NOT NULL,
                                   attribute_id character varying(255) NOT NULL
);


ALTER TABLE public.product_list OWNER TO postgres;

--
-- TOC entry 259 (class 1259 OID 16615)
-- Name: product_urls; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.product_urls (
                                   url_key character varying(100) NOT NULL,
                                   upper_key character varying(100) NOT NULL,
                                   product_id integer NOT NULL
);


ALTER TABLE public.product_urls OWNER TO postgres;

--
-- TOC entry 260 (class 1259 OID 16618)
-- Name: products; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.products (
                               product_id integer NOT NULL,
                               name character varying(250) NOT NULL,
                               short_name character varying(100) NOT NULL,
                               description text NOT NULL,
                               short_description text NOT NULL,
                               sku character varying(255) NOT NULL,
                               ean character varying(255) NOT NULL,
                               manufacturer character varying(255) NOT NULL,
                               location character varying
);


ALTER TABLE public.products OWNER TO postgres;

--
-- TOC entry 261 (class 1259 OID 16623)
-- Name: products_pricing; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.products_pricing (
                                       product_id integer NOT NULL,
                                       price numeric(11,2) NOT NULL,
                                       sale_price numeric(11,2) NOT NULL,
                                       cost_price numeric(11,2) NOT NULL,
                                       tax_class character varying(100) NOT NULL,
                                       sale_date_start character varying(100),
                                       sale_date_end character varying(100)
);


ALTER TABLE public.products_pricing OWNER TO postgres;

--
-- TOC entry 262 (class 1259 OID 16626)
-- Name: products_product_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.products_product_id_seq
  AS integer
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;


ALTER SEQUENCE public.products_product_id_seq OWNER TO postgres;

--
-- TOC entry 3966 (class 0 OID 0)
-- Dependencies: 262
-- Name: products_product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.products_product_id_seq OWNED BY public.products.product_id;


--
-- TOC entry 263 (class 1259 OID 16627)
-- Name: products_seo; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.products_seo (
                                   product_id integer NOT NULL,
                                   meta_title text,
                                   meta_description text,
                                   meta_keywords text,
                                   page_index public.p_index NOT NULL
);


ALTER TABLE public.products_seo OWNER TO postgres;

--
-- TOC entry 264 (class 1259 OID 16632)
-- Name: server_data; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.server_data (
                                  key character varying(45) NOT NULL,
                                  value text NOT NULL
);


ALTER TABLE public.server_data OWNER TO postgres;

--
-- TOC entry 265 (class 1259 OID 16637)
-- Name: server_version; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.server_version (
                                     major integer NOT NULL,
                                     minor integer NOT NULL,
                                     patch integer NOT NULL,
                                     version_name character varying(64) NOT NULL,
                                     version_description text NOT NULL
);


ALTER TABLE public.server_version OWNER TO postgres;

--
-- TOC entry 266 (class 1259 OID 16642)
-- Name: store_view; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.store_view (
                                 store_view_id integer NOT NULL,
                                 website_id integer NOT NULL,
                                 store_view_name character varying(255)
);


ALTER TABLE public.store_view OWNER TO postgres;

--
-- TOC entry 267 (class 1259 OID 16645)
-- Name: store_view_store_view_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.store_view_store_view_id_seq
  AS integer
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;


ALTER SEQUENCE public.store_view_store_view_id_seq OWNER TO postgres;

--
-- TOC entry 3967 (class 0 OID 0)
-- Dependencies: 267
-- Name: store_view_store_view_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.store_view_store_view_id_seq OWNED BY public.store_view.store_view_id;


--
-- TOC entry 268 (class 1259 OID 16646)
-- Name: templates; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.templates (
                                template_key character varying(100) NOT NULL,
                                template_data text NOT NULL,
                                data_string text NOT NULL
);


ALTER TABLE public.templates OWNER TO postgres;

--
-- TOC entry 269 (class 1259 OID 16651)
-- Name: text_page_urls; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.text_page_urls (
                                     url_key character varying(100) NOT NULL,
                                     upper_key character varying(100) NOT NULL,
                                     text_pages_id integer NOT NULL
);


ALTER TABLE public.text_page_urls OWNER TO postgres;

--
-- TOC entry 270 (class 1259 OID 16654)
-- Name: text_pages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.text_pages (
                                 text_pages_id integer NOT NULL,
                                 name character varying(128) NOT NULL,
                                 short_text text NOT NULL,
                                 text text NOT NULL
);


ALTER TABLE public.text_pages OWNER TO postgres;

--
-- TOC entry 271 (class 1259 OID 16659)
-- Name: text_pages_seo; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.text_pages_seo (
                                     text_pages_id integer NOT NULL,
                                     meta_title text,
                                     meta_description text,
                                     meta_keywords text,
                                     page_index public.p_index NOT NULL
);


ALTER TABLE public.text_pages_seo OWNER TO postgres;

--
-- TOC entry 272 (class 1259 OID 16664)
-- Name: text_pages_text_pages_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.text_pages_text_pages_id_seq
  AS integer
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;


ALTER SEQUENCE public.text_pages_text_pages_id_seq OWNER TO postgres;

--
-- TOC entry 3968 (class 0 OID 0)
-- Dependencies: 272
-- Name: text_pages_text_pages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.text_pages_text_pages_id_seq OWNED BY public.text_pages.text_pages_id;


--
-- TOC entry 273 (class 1259 OID 16665)
-- Name: url_keys; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.url_keys (
                               url_key character varying(100) NOT NULL,
                               upper_key character varying(100) NOT NULL,
                               page_type public.p_type NOT NULL
);


ALTER TABLE public.url_keys OWNER TO postgres;

--
-- TOC entry 274 (class 1259 OID 16668)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
                            user_id integer NOT NULL,
                            email character varying(320) NOT NULL,
                            password character varying(100) NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 275 (class 1259 OID 16671)
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_user_id_seq
  AS integer
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;


ALTER SEQUENCE public.users_user_id_seq OWNER TO postgres;

--
-- TOC entry 3969 (class 0 OID 0)
-- Dependencies: 275
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- TOC entry 276 (class 1259 OID 16672)
-- Name: websites; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.websites (
                               website_id integer NOT NULL,
                               website_name character varying(255)
);


ALTER TABLE public.websites OWNER TO postgres;

--
-- TOC entry 277 (class 1259 OID 16675)
-- Name: websites_website_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.websites_website_id_seq
  AS integer
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;


ALTER SEQUENCE public.websites_website_id_seq OWNER TO postgres;

--
-- TOC entry 3970 (class 0 OID 0)
-- Dependencies: 277
-- Name: websites_website_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.websites_website_id_seq OWNED BY public.websites.website_id;


--
-- TOC entry 3496 (class 2604 OID 16676)
-- Name: backend_block block_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.backend_block ALTER COLUMN block_id SET DEFAULT nextval('public.backend_block_block_id_seq'::regclass);


--
-- TOC entry 3497 (class 2604 OID 16677)
-- Name: categories category_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories ALTER COLUMN category_id SET DEFAULT nextval('public.categories_category_id_seq'::regclass);


--
-- TOC entry 3498 (class 2604 OID 16678)
-- Name: list_items list_item_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.list_items ALTER COLUMN list_item_id SET DEFAULT nextval('public.list_item_list_item_id_seq'::regclass);


--
-- TOC entry 3500 (class 2604 OID 16679)
-- Name: products product_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products ALTER COLUMN product_id SET DEFAULT nextval('public.products_product_id_seq'::regclass);


--
-- TOC entry 3501 (class 2604 OID 16680)
-- Name: store_view store_view_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.store_view ALTER COLUMN store_view_id SET DEFAULT nextval('public.store_view_store_view_id_seq'::regclass);


--
-- TOC entry 3502 (class 2604 OID 16681)
-- Name: text_pages text_pages_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_pages ALTER COLUMN text_pages_id SET DEFAULT nextval('public.text_pages_text_pages_id_seq'::regclass);


--
-- TOC entry 3503 (class 2604 OID 16682)
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- TOC entry 3504 (class 2604 OID 16683)
-- Name: websites website_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.websites ALTER COLUMN website_id SET DEFAULT nextval('public.websites_website_id_seq'::regclass);


--
-- TOC entry 3888 (class 0 OID 16436)
-- Dependencies: 209
-- Data for Name: attribute_block; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.attribute_block VALUES (4, 'product_sku');
INSERT INTO public.attribute_block VALUES (4, 'product_location');
INSERT INTO public.attribute_block VALUES (4, 'product_ean');
INSERT INTO public.attribute_block VALUES (4, 'product_manufacturer');
INSERT INTO public.attribute_block VALUES (5, 'product_description');
INSERT INTO public.attribute_block VALUES (5, 'product_short_description');
INSERT INTO public.attribute_block VALUES (6, 'product_price');
INSERT INTO public.attribute_block VALUES (6, 'product_sale_price');
INSERT INTO public.attribute_block VALUES (6, 'product_sale_dates');
INSERT INTO public.attribute_block VALUES (4, 'product_categories');
INSERT INTO public.attribute_block VALUES (7, 'images');


--
-- TOC entry 3889 (class 0 OID 16439)
-- Dependencies: 210
-- Data for Name: attribute_list; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.attribute_list VALUES ('product_sale_dates', 'product_sale_dates');
INSERT INTO public.attribute_list VALUES ('product_categories', 'product_categories');


--
-- TOC entry 3890 (class 0 OID 16444)
-- Dependencies: 211
-- Data for Name: backend_block; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.backend_block VALUES (4, 'Id data', 'id_information');
INSERT INTO public.backend_block VALUES (5, 'Content', 'standard');
INSERT INTO public.backend_block VALUES (6, 'Price', 'product_price');
INSERT INTO public.backend_block VALUES (7, 'Images', 'images');


--
-- TOC entry 3892 (class 0 OID 16450)
-- Dependencies: 213
-- Data for Name: backend_permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.backend_permissions VALUES (2, 'read-write', 'read-write', 'read-write', 'read-write', 'read-write', 'read-write', 'read-write', 'read-write', 'read-write', NULL);


--
-- TOC entry 3893 (class 0 OID 16453)
-- Dependencies: 214
-- Data for Name: block_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.block_attributes VALUES ('product_sku', 'SKU', 'text');
INSERT INTO public.block_attributes VALUES ('product_location', 'Location', 'text');
INSERT INTO public.block_attributes VALUES ('product_ean', 'EAN', 'text');
INSERT INTO public.block_attributes VALUES ('product_manufacturer', 'Manufacturer', 'text');
INSERT INTO public.block_attributes VALUES ('product_description', 'Description', 'wysiwyg');
INSERT INTO public.block_attributes VALUES ('product_short_description', 'Short description', 'wysiwyg');
INSERT INTO public.block_attributes VALUES ('product_cost_price', 'Cost price', 'price');
INSERT INTO public.block_attributes VALUES ('product_tax_class', 'Tax class', 'text');
INSERT INTO public.block_attributes VALUES ('product_price', 'Price', 'price');
INSERT INTO public.block_attributes VALUES ('product_sale_price', 'Sale price', 'price');
INSERT INTO public.block_attributes VALUES ('product_sale_dates', 'Sale dates', 'list');
INSERT INTO public.block_attributes VALUES ('product_categories', 'Categories', 'list');
INSERT INTO public.block_attributes VALUES ('images', 'images', 'images');


--
-- TOC entry 3894 (class 0 OID 16456)
-- Dependencies: 215
-- Data for Name: block_id; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3895 (class 0 OID 16459)
-- Dependencies: 216
-- Data for Name: blocks; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3896 (class 0 OID 16462)
-- Dependencies: 217
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3898 (class 0 OID 16468)
-- Dependencies: 219
-- Data for Name: categories_products; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3899 (class 0 OID 16471)
-- Dependencies: 220
-- Data for Name: categories_seo; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3900 (class 0 OID 16476)
-- Dependencies: 221
-- Data for Name: category_urls; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3901 (class 0 OID 16479)
-- Dependencies: 222
-- Data for Name: custom_product_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3902 (class 0 OID 16482)
-- Dependencies: 223
-- Data for Name: eav; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3903 (class 0 OID 16485)
-- Dependencies: 224
-- Data for Name: eav_attribute_bool; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3904 (class 0 OID 16490)
-- Dependencies: 225
-- Data for Name: eav_attribute_float; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3905 (class 0 OID 16495)
-- Dependencies: 226
-- Data for Name: eav_attribute_int; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3906 (class 0 OID 16500)
-- Dependencies: 227
-- Data for Name: eav_attribute_money; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3907 (class 0 OID 16505)
-- Dependencies: 228
-- Data for Name: eav_attribute_multi_select; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3908 (class 0 OID 16510)
-- Dependencies: 229
-- Data for Name: eav_attribute_string; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3909 (class 0 OID 16515)
-- Dependencies: 230
-- Data for Name: eav_global_bool; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3910 (class 0 OID 16518)
-- Dependencies: 231
-- Data for Name: eav_global_float; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3911 (class 0 OID 16521)
-- Dependencies: 232
-- Data for Name: eav_global_int; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3912 (class 0 OID 16524)
-- Dependencies: 233
-- Data for Name: eav_global_money; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3913 (class 0 OID 16527)
-- Dependencies: 234
-- Data for Name: eav_global_multi_select; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3914 (class 0 OID 16530)
-- Dependencies: 235
-- Data for Name: eav_global_string; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3915 (class 0 OID 16535)
-- Dependencies: 236
-- Data for Name: eav_store_view_bool; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3916 (class 0 OID 16538)
-- Dependencies: 237
-- Data for Name: eav_store_view_float; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3917 (class 0 OID 16541)
-- Dependencies: 238
-- Data for Name: eav_store_view_int; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3918 (class 0 OID 16544)
-- Dependencies: 239
-- Data for Name: eav_store_view_money; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3919 (class 0 OID 16547)
-- Dependencies: 240
-- Data for Name: eav_store_view_multi_select; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3920 (class 0 OID 16550)
-- Dependencies: 241
-- Data for Name: eav_store_view_string; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3921 (class 0 OID 16555)
-- Dependencies: 242
-- Data for Name: eav_website_bool; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3922 (class 0 OID 16558)
-- Dependencies: 243
-- Data for Name: eav_website_float; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3923 (class 0 OID 16561)
-- Dependencies: 244
-- Data for Name: eav_website_int; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3924 (class 0 OID 16564)
-- Dependencies: 245
-- Data for Name: eav_website_money; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3925 (class 0 OID 16567)
-- Dependencies: 246
-- Data for Name: eav_website_multi_select; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3926 (class 0 OID 16570)
-- Dependencies: 247
-- Data for Name: eav_website_string; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3927 (class 0 OID 16575)
-- Dependencies: 248
-- Data for Name: image; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.image VALUES ('https://picsum.photos/200/1000', 'test-image', 'jpg,png,webp');
INSERT INTO public.image VALUES ('https://picsum.photos/200/1000', 'test 1', '["png", "jpg"]');
INSERT INTO public.image VALUES ('https://picsum.photos/200/1000', 'test 2', '["png"," jpg", "webp"]');


--
-- TOC entry 3928 (class 0 OID 16580)
-- Dependencies: 249
-- Data for Name: image_product; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.image_product VALUES (1, 'https://picsum.photos/200/1000', 'test 1');
INSERT INTO public.image_product VALUES (1, 'https://picsum.photos/200/1000', 'test 2');


--
-- TOC entry 3929 (class 0 OID 16585)
-- Dependencies: 250
-- Data for Name: list_items; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.list_items VALUES (6, 'product_categories', 0, NULL, NULL, NULL, NULL, NULL, 'category 1');
INSERT INTO public.list_items VALUES (7, 'product_categories', 0, NULL, NULL, NULL, NULL, NULL, 'category 2');
INSERT INTO public.list_items VALUES (8, 'product_categories', 0, NULL, NULL, NULL, NULL, NULL, 'category 3');
INSERT INTO public.list_items VALUES (1, 'product_sale_dates', 0, NULL, NULL, NULL, NULL, NULL, '2025-01-01');
INSERT INTO public.list_items VALUES (2, 'product_sale_dates', 0, NULL, NULL, NULL, NULL, NULL, '2025-01-10');


--
-- TOC entry 3931 (class 0 OID 16592)
-- Dependencies: 252
-- Data for Name: multi_select_attributes_bool; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3932 (class 0 OID 16595)
-- Dependencies: 253
-- Data for Name: multi_select_attributes_float; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3933 (class 0 OID 16598)
-- Dependencies: 254
-- Data for Name: multi_select_attributes_int; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3934 (class 0 OID 16601)
-- Dependencies: 255
-- Data for Name: multi_select_attributes_money; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3935 (class 0 OID 16604)
-- Dependencies: 256
-- Data for Name: multi_select_attributes_string; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3936 (class 0 OID 16609)
-- Dependencies: 257
-- Data for Name: page_block; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.page_block VALUES ('product_info', 4);
INSERT INTO public.page_block VALUES ('product_info', 5);
INSERT INTO public.page_block VALUES ('product_info', 6);
INSERT INTO public.page_block VALUES ('product_info', 7);


--
-- TOC entry 3937 (class 0 OID 16612)
-- Dependencies: 258
-- Data for Name: product_list; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.product_list VALUES (1, 1, 'product_sale_dates');
INSERT INTO public.product_list VALUES (1, 2, 'product_sale_dates');
INSERT INTO public.product_list VALUES (1, 6, 'product_categories');
INSERT INTO public.product_list VALUES (1, 7, 'product_categories');
INSERT INTO public.product_list VALUES (1, 8, 'product_categories');


--
-- TOC entry 3938 (class 0 OID 16615)
-- Dependencies: 259
-- Data for Name: product_urls; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3939 (class 0 OID 16618)
-- Dependencies: 260
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.products VALUES (1, 'test', 't', 'desc', 'd', '123', '456', 'man', 'test_location');
INSERT INTO public.products VALUES (2, 'testProduct', 'testProduct', 'This is a test product', 'A test product for testing purposes', 'test-sku', 'test-ean', 'test-manufacturer', 'test-location');


--
-- TOC entry 3940 (class 0 OID 16623)
-- Dependencies: 261
-- Data for Name: products_pricing; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.products_pricing VALUES (1, 12.99, 9.99, 4.99, 'test tax class', '01-01-2025', '10-01-2025');


--
-- TOC entry 3942 (class 0 OID 16627)
-- Dependencies: 263
-- Data for Name: products_seo; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.products_seo VALUES (2, 'Test Product - Meta Title', 'Test Product - Meta Description', 'Test Product - Meta Keywords', 'index, follow');
INSERT INTO public.products_seo VALUES (1, 'Test Product - Meta Title', 'Test Product - Meta Description', 'Test Product - Meta Keywords', 'index, follow');


--
-- TOC entry 3943 (class 0 OID 16632)
-- Dependencies: 264
-- Data for Name: server_data; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3944 (class 0 OID 16637)
-- Dependencies: 265
-- Data for Name: server_version; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3945 (class 0 OID 16642)
-- Dependencies: 266
-- Data for Name: store_view; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3947 (class 0 OID 16646)
-- Dependencies: 268
-- Data for Name: templates; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.templates VALUES ('testKey', '<test>testData</test>', '');


--
-- TOC entry 3948 (class 0 OID 16651)
-- Dependencies: 269
-- Data for Name: text_page_urls; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3949 (class 0 OID 16654)
-- Dependencies: 270
-- Data for Name: text_pages; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3950 (class 0 OID 16659)
-- Dependencies: 271
-- Data for Name: text_pages_seo; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3952 (class 0 OID 16665)
-- Dependencies: 273
-- Data for Name: url_keys; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3953 (class 0 OID 16668)
-- Dependencies: 274
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.users VALUES (2, 'test@test.com', '$2a$12$/jox6so5ZP45PtbitQtaROzElc0Ri4A1oyWoeU6j//S5ZPlSQ43OK');


--
-- TOC entry 3955 (class 0 OID 16672)
-- Dependencies: 276
-- Data for Name: websites; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3971 (class 0 OID 0)
-- Dependencies: 212
-- Name: backend_block_block_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.backend_block_block_id_seq', 7, true);


--
-- TOC entry 3972 (class 0 OID 0)
-- Dependencies: 218
-- Name: categories_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.categories_category_id_seq', 1, false);


--
-- TOC entry 3973 (class 0 OID 0)
-- Dependencies: 251
-- Name: list_item_list_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.list_item_list_item_id_seq', 8, true);


--
-- TOC entry 3974 (class 0 OID 0)
-- Dependencies: 262
-- Name: products_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.products_product_id_seq', 2, true);


--
-- TOC entry 3975 (class 0 OID 0)
-- Dependencies: 267
-- Name: store_view_store_view_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.store_view_store_view_id_seq', 1, false);


--
-- TOC entry 3976 (class 0 OID 0)
-- Dependencies: 272
-- Name: text_pages_text_pages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.text_pages_text_pages_id_seq', 1, false);


--
-- TOC entry 3977 (class 0 OID 0)
-- Dependencies: 275
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 2, true);


--
-- TOC entry 3978 (class 0 OID 0)
-- Dependencies: 277
-- Name: websites_website_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.websites_website_id_seq', 1, false);


--
-- TOC entry 3648 (class 2606 OID 16685)
-- Name: url_keys UK_1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.url_keys
  ADD CONSTRAINT "UK_1" UNIQUE (url_key);


--
-- TOC entry 3506 (class 2606 OID 16687)
-- Name: attribute_block attribute_block_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attribute_block
  ADD CONSTRAINT attribute_block_pkey PRIMARY KEY (block_id, attribute_id);


--
-- TOC entry 3508 (class 2606 OID 16689)
-- Name: attribute_list attribute_list_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attribute_list
  ADD CONSTRAINT attribute_list_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 3510 (class 2606 OID 16691)
-- Name: backend_block backend_block_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.backend_block
  ADD CONSTRAINT backend_block_pkey PRIMARY KEY (block_id);


--
-- TOC entry 3513 (class 2606 OID 16693)
-- Name: block_attributes block_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.block_attributes
  ADD CONSTRAINT block_attributes_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 3515 (class 2606 OID 16695)
-- Name: block_id block_id_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.block_id
  ADD CONSTRAINT block_id_pkey PRIMARY KEY (block_id, category_id, product_id);


--
-- TOC entry 3517 (class 2606 OID 16697)
-- Name: blocks blocks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blocks
  ADD CONSTRAINT blocks_pkey PRIMARY KEY (template_key);


--
-- TOC entry 3520 (class 2606 OID 16699)
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
  ADD CONSTRAINT categories_pkey PRIMARY KEY (category_id);


--
-- TOC entry 3523 (class 2606 OID 16701)
-- Name: categories_products categories_products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories_products
  ADD CONSTRAINT categories_products_pkey PRIMARY KEY (category_id, product_id);


--
-- TOC entry 3525 (class 2606 OID 16703)
-- Name: categories_seo categories_seo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories_seo
  ADD CONSTRAINT categories_seo_pkey PRIMARY KEY (category_id);


--
-- TOC entry 3528 (class 2606 OID 16705)
-- Name: category_urls category_urls_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category_urls
  ADD CONSTRAINT category_urls_pkey PRIMARY KEY (url_key, upper_key);


--
-- TOC entry 3531 (class 2606 OID 16707)
-- Name: custom_product_attributes custom_product_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_product_attributes
  ADD CONSTRAINT custom_product_attributes_pkey PRIMARY KEY (attribute_key);


--
-- TOC entry 3537 (class 2606 OID 16709)
-- Name: eav_attribute_bool eav_attribute_bool_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_bool
  ADD CONSTRAINT eav_attribute_bool_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3539 (class 2606 OID 16711)
-- Name: eav_attribute_float eav_attribute_float_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_float
  ADD CONSTRAINT eav_attribute_float_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3541 (class 2606 OID 16713)
-- Name: eav_attribute_int eav_attribute_int_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_int
  ADD CONSTRAINT eav_attribute_int_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3543 (class 2606 OID 16715)
-- Name: eav_attribute_money eav_attribute_money_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_money
  ADD CONSTRAINT eav_attribute_money_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3545 (class 2606 OID 16717)
-- Name: eav_attribute_multi_select eav_attribute_multi_select_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_multi_select
  ADD CONSTRAINT eav_attribute_multi_select_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3547 (class 2606 OID 16719)
-- Name: eav_attribute_string eav_attribute_string_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_string
  ADD CONSTRAINT eav_attribute_string_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3550 (class 2606 OID 16721)
-- Name: eav_global_bool eav_global_bool_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_bool
  ADD CONSTRAINT eav_global_bool_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3553 (class 2606 OID 16723)
-- Name: eav_global_float eav_global_float_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_float
  ADD CONSTRAINT eav_global_float_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3556 (class 2606 OID 16725)
-- Name: eav_global_int eav_global_int_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_int
  ADD CONSTRAINT eav_global_int_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3559 (class 2606 OID 16727)
-- Name: eav_global_money eav_global_money_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_money
  ADD CONSTRAINT eav_global_money_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3562 (class 2606 OID 16729)
-- Name: eav_global_multi_select eav_global_multi_select_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_multi_select
  ADD CONSTRAINT eav_global_multi_select_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3565 (class 2606 OID 16731)
-- Name: eav_global_string eav_global_string_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_string
  ADD CONSTRAINT eav_global_string_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3534 (class 2606 OID 16733)
-- Name: eav eav_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav
  ADD CONSTRAINT eav_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3568 (class 2606 OID 16735)
-- Name: eav_store_view_bool eav_store_view_bool_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_bool
  ADD CONSTRAINT eav_store_view_bool_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3572 (class 2606 OID 16737)
-- Name: eav_store_view_float eav_store_view_float_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_float
  ADD CONSTRAINT eav_store_view_float_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3575 (class 2606 OID 16739)
-- Name: eav_store_view_int eav_store_view_int_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_int
  ADD CONSTRAINT eav_store_view_int_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3578 (class 2606 OID 16741)
-- Name: eav_store_view_money eav_store_view_money_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_money
  ADD CONSTRAINT eav_store_view_money_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3581 (class 2606 OID 16743)
-- Name: eav_store_view_multi_select eav_store_view_multi_select_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_multi_select
  ADD CONSTRAINT eav_store_view_multi_select_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3584 (class 2606 OID 16745)
-- Name: eav_store_view_string eav_store_view_string_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_string
  ADD CONSTRAINT eav_store_view_string_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3587 (class 2606 OID 16747)
-- Name: eav_website_bool eav_website_bool_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_bool
  ADD CONSTRAINT eav_website_bool_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3590 (class 2606 OID 16749)
-- Name: eav_website_float eav_website_float_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_float
  ADD CONSTRAINT eav_website_float_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3593 (class 2606 OID 16751)
-- Name: eav_website_int eav_website_int_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_int
  ADD CONSTRAINT eav_website_int_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3596 (class 2606 OID 16753)
-- Name: eav_website_money eav_website_money_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_money
  ADD CONSTRAINT eav_website_money_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3599 (class 2606 OID 16755)
-- Name: eav_website_multi_select eav_website_multi_select_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_multi_select
  ADD CONSTRAINT eav_website_multi_select_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3602 (class 2606 OID 16757)
-- Name: eav_website_string eav_website_string_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_string
  ADD CONSTRAINT eav_website_string_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3604 (class 2606 OID 16759)
-- Name: image image_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.image
  ADD CONSTRAINT image_pkey PRIMARY KEY (image_url, image_name);


--
-- TOC entry 3606 (class 2606 OID 16761)
-- Name: image_product image_product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.image_product
  ADD CONSTRAINT image_product_pkey PRIMARY KEY (product_id, image_url, image_name);


--
-- TOC entry 3608 (class 2606 OID 16763)
-- Name: list_items list_items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.list_items
  ADD CONSTRAINT list_items_pkey PRIMARY KEY (list_item_id);


--
-- TOC entry 3610 (class 2606 OID 16765)
-- Name: multi_select_attributes_bool multi_select_attributes_bool_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_bool
  ADD CONSTRAINT multi_select_attributes_bool_pkey PRIMARY KEY (attribute_key, option);


--
-- TOC entry 3612 (class 2606 OID 16767)
-- Name: multi_select_attributes_float multi_select_attributes_float_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_float
  ADD CONSTRAINT multi_select_attributes_float_pkey PRIMARY KEY (attribute_key, option);


--
-- TOC entry 3614 (class 2606 OID 16769)
-- Name: multi_select_attributes_int multi_select_attributes_int_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_int
  ADD CONSTRAINT multi_select_attributes_int_pkey PRIMARY KEY (attribute_key, option);


--
-- TOC entry 3616 (class 2606 OID 16771)
-- Name: multi_select_attributes_money multi_select_attributes_money_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_money
  ADD CONSTRAINT multi_select_attributes_money_pkey PRIMARY KEY (attribute_key, option);


--
-- TOC entry 3618 (class 2606 OID 16773)
-- Name: multi_select_attributes_string multi_select_attributes_string_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_string
  ADD CONSTRAINT multi_select_attributes_string_pkey PRIMARY KEY (attribute_key, option);


--
-- TOC entry 3620 (class 2606 OID 16775)
-- Name: page_block page_block_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.page_block
  ADD CONSTRAINT page_block_pkey PRIMARY KEY (page_name, block_id);


--
-- TOC entry 3622 (class 2606 OID 16777)
-- Name: product_list product_list_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_list
  ADD CONSTRAINT product_list_pkey PRIMARY KEY (product_id, list_item_id, attribute_id);


--
-- TOC entry 3624 (class 2606 OID 16779)
-- Name: product_urls product_urls_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_urls
  ADD CONSTRAINT product_urls_pkey PRIMARY KEY (url_key, upper_key);


--
-- TOC entry 3627 (class 2606 OID 16781)
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
  ADD CONSTRAINT products_pkey PRIMARY KEY (product_id);


--
-- TOC entry 3629 (class 2606 OID 16783)
-- Name: products_pricing products_pricing_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_pricing
  ADD CONSTRAINT products_pricing_pkey PRIMARY KEY (product_id);


--
-- TOC entry 3631 (class 2606 OID 16785)
-- Name: products_seo products_seo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_seo
  ADD CONSTRAINT products_seo_pkey PRIMARY KEY (product_id);


--
-- TOC entry 3633 (class 2606 OID 16787)
-- Name: server_data server_data_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_data
  ADD CONSTRAINT server_data_pkey PRIMARY KEY (key);


--
-- TOC entry 3635 (class 2606 OID 16789)
-- Name: server_version server_version_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_version
  ADD CONSTRAINT server_version_pkey PRIMARY KEY (major, minor, patch);


--
-- TOC entry 3637 (class 2606 OID 16791)
-- Name: store_view store_view_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.store_view
  ADD CONSTRAINT store_view_pkey PRIMARY KEY (store_view_id);


--
-- TOC entry 3640 (class 2606 OID 16793)
-- Name: templates templates_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.templates
  ADD CONSTRAINT templates_pkey PRIMARY KEY (template_key);


--
-- TOC entry 3642 (class 2606 OID 16795)
-- Name: text_page_urls text_page_urls_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_page_urls
  ADD CONSTRAINT text_page_urls_pkey PRIMARY KEY (url_key, upper_key);


--
-- TOC entry 3644 (class 2606 OID 16797)
-- Name: text_pages text_pages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_pages
  ADD CONSTRAINT text_pages_pkey PRIMARY KEY (text_pages_id);


--
-- TOC entry 3646 (class 2606 OID 16799)
-- Name: text_pages_seo text_pages_seo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_pages_seo
  ADD CONSTRAINT text_pages_seo_pkey PRIMARY KEY (text_pages_id);


--
-- TOC entry 3651 (class 2606 OID 16801)
-- Name: url_keys url_keys_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.url_keys
  ADD CONSTRAINT url_keys_pkey PRIMARY KEY (url_key, upper_key);


--
-- TOC entry 3653 (class 2606 OID 16803)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
  ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 3655 (class 2606 OID 16805)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
  ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3657 (class 2606 OID 16807)
-- Name: websites websites_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.websites
  ADD CONSTRAINT websites_pkey PRIMARY KEY (website_id);


--
-- TOC entry 3625 (class 1259 OID 17270)
-- Name: I1; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I1" ON public.products USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3570 (class 1259 OID 17279)
-- Name: I10; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I10" ON public.eav_store_view_float USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3573 (class 1259 OID 17280)
-- Name: I11; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I11" ON public.eav_store_view_int USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3576 (class 1259 OID 17281)
-- Name: I12; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I12" ON public.eav_store_view_money USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3579 (class 1259 OID 17282)
-- Name: I13; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I13" ON public.eav_store_view_multi_select USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3582 (class 1259 OID 17283)
-- Name: I14; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I14" ON public.eav_store_view_string USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3585 (class 1259 OID 17284)
-- Name: I15; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I15" ON public.eav_website_bool USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3588 (class 1259 OID 17285)
-- Name: I16; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I16" ON public.eav_website_float USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3591 (class 1259 OID 17286)
-- Name: I17; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I17" ON public.eav_website_int USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3594 (class 1259 OID 17287)
-- Name: I18; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I18" ON public.eav_website_money USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3597 (class 1259 OID 17288)
-- Name: I19; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I19" ON public.eav_website_multi_select USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3532 (class 1259 OID 17271)
-- Name: I2; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I2" ON public.eav USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3600 (class 1259 OID 17289)
-- Name: I20; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I20" ON public.eav_website_string USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3529 (class 1259 OID 17290)
-- Name: I21; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I21" ON public.custom_product_attributes USING btree (attribute_key) WITH (deduplicate_items='true');


--
-- TOC entry 3548 (class 1259 OID 17272)
-- Name: I3; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I3" ON public.eav_global_bool USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3551 (class 1259 OID 17273)
-- Name: I4; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I4" ON public.eav_global_float USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3554 (class 1259 OID 17274)
-- Name: I5; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I5" ON public.eav_global_int USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3557 (class 1259 OID 17275)
-- Name: I6; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I6" ON public.eav_global_money USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3560 (class 1259 OID 17276)
-- Name: I7; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I7" ON public.eav_global_multi_select USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3563 (class 1259 OID 17277)
-- Name: I8; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I8" ON public.eav_global_string USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3566 (class 1259 OID 17278)
-- Name: I9; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "I9" ON public.eav_store_view_bool USING btree (product_id) WITH (deduplicate_items='true');


--
-- TOC entry 3511 (class 1259 OID 16808)
-- Name: fki_FK_1; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_1" ON public.backend_permissions USING btree (user_id);


--
-- TOC entry 3535 (class 1259 OID 16809)
-- Name: fki_FK_2; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_2" ON public.eav USING btree (attribute_key);


--
-- TOC entry 3569 (class 1259 OID 16810)
-- Name: fki_FK_3; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_3" ON public.eav_store_view_bool USING btree (attribute_key);


--
-- TOC entry 3526 (class 1259 OID 16811)
-- Name: fki_FK_61; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_61" ON public.categories_seo USING btree (category_id);


--
-- TOC entry 3649 (class 1259 OID 16812)
-- Name: fki_FK_62; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_62" ON public.url_keys USING btree (upper_key);


--
-- TOC entry 3521 (class 1259 OID 16813)
-- Name: fki_FK_67; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_67" ON public.categories USING btree (upper_category);


--
-- TOC entry 3638 (class 1259 OID 16814)
-- Name: fki_FK_72; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_72" ON public.templates USING btree (template_key);


--
-- TOC entry 3518 (class 1259 OID 16815)
-- Name: fki_FK_73; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_73" ON public.blocks USING btree (template_key);


--
-- TOC entry 3748 (class 2620 OID 16816)
-- Name: url_keys after_insert_url_keys; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER after_insert_url_keys AFTER INSERT ON public.url_keys FOR EACH ROW EXECUTE FUNCTION public.check_root_url();


--
-- TOC entry 3661 (class 2606 OID 16817)
-- Name: backend_permissions FK_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.backend_permissions
  ADD CONSTRAINT "FK_1" FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 3733 (class 2606 OID 16822)
-- Name: multi_select_attributes_money FK_10; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_money
  ADD CONSTRAINT "FK_10" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3730 (class 2606 OID 16827)
-- Name: multi_select_attributes_bool FK_11; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_bool
  ADD CONSTRAINT "FK_11" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3690 (class 2606 OID 16832)
-- Name: eav_global_string FK_12; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_string
  ADD CONSTRAINT "FK_12" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3691 (class 2606 OID 16837)
-- Name: eav_global_string FK_13; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_string
  ADD CONSTRAINT "FK_13" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3684 (class 2606 OID 16842)
-- Name: eav_global_int FK_14; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_int
  ADD CONSTRAINT "FK_14" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3685 (class 2606 OID 16847)
-- Name: eav_global_int FK_15; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_int
  ADD CONSTRAINT "FK_15" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3682 (class 2606 OID 16852)
-- Name: eav_global_float FK_16; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_float
  ADD CONSTRAINT "FK_16" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3683 (class 2606 OID 16857)
-- Name: eav_global_float FK_17; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_float
  ADD CONSTRAINT "FK_17" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3686 (class 2606 OID 16862)
-- Name: eav_global_money FK_18; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_money
  ADD CONSTRAINT "FK_18" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3687 (class 2606 OID 16867)
-- Name: eav_global_money FK_19; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_money
  ADD CONSTRAINT "FK_19" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3672 (class 2606 OID 16872)
-- Name: eav FK_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav
  ADD CONSTRAINT "FK_2" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3680 (class 2606 OID 16877)
-- Name: eav_global_bool FK_20; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_bool
  ADD CONSTRAINT "FK_20" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3681 (class 2606 OID 16882)
-- Name: eav_global_bool FK_21; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_bool
  ADD CONSTRAINT "FK_21" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3688 (class 2606 OID 16887)
-- Name: eav_global_multi_select FK_22; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_multi_select
  ADD CONSTRAINT "FK_22" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3743 (class 2606 OID 16892)
-- Name: store_view FK_22_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.store_view
  ADD CONSTRAINT "FK_22_1" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3689 (class 2606 OID 16897)
-- Name: eav_global_multi_select FK_23; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_multi_select
  ADD CONSTRAINT "FK_23" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3710 (class 2606 OID 16902)
-- Name: eav_website_bool FK_23_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_bool
  ADD CONSTRAINT "FK_23_1" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3711 (class 2606 OID 16907)
-- Name: eav_website_bool FK_24; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_bool
  ADD CONSTRAINT "FK_24" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3712 (class 2606 OID 16912)
-- Name: eav_website_bool FK_25; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_bool
  ADD CONSTRAINT "FK_25" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3713 (class 2606 OID 16917)
-- Name: eav_website_float FK_26; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_float
  ADD CONSTRAINT "FK_26" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3714 (class 2606 OID 16922)
-- Name: eav_website_float FK_27; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_float
  ADD CONSTRAINT "FK_27" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3715 (class 2606 OID 16927)
-- Name: eav_website_float FK_28; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_float
  ADD CONSTRAINT "FK_28" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3716 (class 2606 OID 16932)
-- Name: eav_website_int FK_29; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_int
  ADD CONSTRAINT "FK_29" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3673 (class 2606 OID 16937)
-- Name: eav FK_3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav
  ADD CONSTRAINT "FK_3" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3717 (class 2606 OID 16942)
-- Name: eav_website_int FK_30; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_int
  ADD CONSTRAINT "FK_30" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3718 (class 2606 OID 16947)
-- Name: eav_website_int FK_31; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_int
  ADD CONSTRAINT "FK_31" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3719 (class 2606 OID 16952)
-- Name: eav_website_money FK_32; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_money
  ADD CONSTRAINT "FK_32" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3720 (class 2606 OID 16957)
-- Name: eav_website_money FK_33; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_money
  ADD CONSTRAINT "FK_33" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3721 (class 2606 OID 16962)
-- Name: eav_website_money FK_34; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_money
  ADD CONSTRAINT "FK_34" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3722 (class 2606 OID 16967)
-- Name: eav_website_multi_select FK_35; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_multi_select
  ADD CONSTRAINT "FK_35" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3723 (class 2606 OID 16972)
-- Name: eav_website_multi_select FK_36; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_multi_select
  ADD CONSTRAINT "FK_36" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3724 (class 2606 OID 16977)
-- Name: eav_website_multi_select FK_37; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_multi_select
  ADD CONSTRAINT "FK_37" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3725 (class 2606 OID 16982)
-- Name: eav_website_string FK_38; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_string
  ADD CONSTRAINT "FK_38" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3726 (class 2606 OID 16987)
-- Name: eav_website_string FK_39; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_string
  ADD CONSTRAINT "FK_39" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3727 (class 2606 OID 16992)
-- Name: eav_website_string FK_40; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_string
  ADD CONSTRAINT "FK_40" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3692 (class 2606 OID 16997)
-- Name: eav_store_view_bool FK_41; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_bool
  ADD CONSTRAINT "FK_41" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3693 (class 2606 OID 17002)
-- Name: eav_store_view_bool FK_42; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_bool
  ADD CONSTRAINT "FK_42" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3694 (class 2606 OID 17007)
-- Name: eav_store_view_bool FK_43; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_bool
  ADD CONSTRAINT "FK_43" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3695 (class 2606 OID 17012)
-- Name: eav_store_view_float FK_44; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_float
  ADD CONSTRAINT "FK_44" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3696 (class 2606 OID 17017)
-- Name: eav_store_view_float FK_45; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_float
  ADD CONSTRAINT "FK_45" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3697 (class 2606 OID 17022)
-- Name: eav_store_view_float FK_46; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_float
  ADD CONSTRAINT "FK_46" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3698 (class 2606 OID 17027)
-- Name: eav_store_view_int FK_47; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_int
  ADD CONSTRAINT "FK_47" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3699 (class 2606 OID 17032)
-- Name: eav_store_view_int FK_48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_int
  ADD CONSTRAINT "FK_48" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3700 (class 2606 OID 17037)
-- Name: eav_store_view_int FK_49; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_int
  ADD CONSTRAINT "FK_49" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3701 (class 2606 OID 17042)
-- Name: eav_store_view_money FK_50; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_money
  ADD CONSTRAINT "FK_50" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3702 (class 2606 OID 17047)
-- Name: eav_store_view_money FK_51; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_money
  ADD CONSTRAINT "FK_51" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3703 (class 2606 OID 17052)
-- Name: eav_store_view_money FK_52; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_money
  ADD CONSTRAINT "FK_52" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3704 (class 2606 OID 17057)
-- Name: eav_store_view_multi_select FK_53; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_multi_select
  ADD CONSTRAINT "FK_53" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3705 (class 2606 OID 17062)
-- Name: eav_store_view_multi_select FK_54; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_multi_select
  ADD CONSTRAINT "FK_54" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3706 (class 2606 OID 17067)
-- Name: eav_store_view_multi_select FK_55; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_multi_select
  ADD CONSTRAINT "FK_55" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3707 (class 2606 OID 17072)
-- Name: eav_store_view_string FK_56; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_string
  ADD CONSTRAINT "FK_56" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3708 (class 2606 OID 17077)
-- Name: eav_store_view_string FK_57; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_string
  ADD CONSTRAINT "FK_57" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3709 (class 2606 OID 17082)
-- Name: eav_store_view_string FK_58; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_string
  ADD CONSTRAINT "FK_58" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3667 (class 2606 OID 17087)
-- Name: categories_products FK_59; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories_products
  ADD CONSTRAINT "FK_59" FOREIGN KEY (category_id) REFERENCES public.categories(category_id);


--
-- TOC entry 3734 (class 2606 OID 17092)
-- Name: multi_select_attributes_string FK_6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_string
  ADD CONSTRAINT "FK_6" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3668 (class 2606 OID 17097)
-- Name: categories_products FK_60; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories_products
  ADD CONSTRAINT "FK_60" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3669 (class 2606 OID 17102)
-- Name: categories_seo FK_61; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories_seo
  ADD CONSTRAINT "FK_61" FOREIGN KEY (category_id) REFERENCES public.categories(category_id);


--
-- TOC entry 3747 (class 2606 OID 17107)
-- Name: url_keys FK_62; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.url_keys
  ADD CONSTRAINT "FK_62" FOREIGN KEY (upper_key) REFERENCES public.url_keys(url_key);


--
-- TOC entry 3739 (class 2606 OID 17112)
-- Name: product_urls FK_63; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_urls
  ADD CONSTRAINT "FK_63" FOREIGN KEY (url_key, upper_key) REFERENCES public.url_keys(url_key, upper_key);


--
-- TOC entry 3740 (class 2606 OID 17117)
-- Name: product_urls FK_64; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_urls
  ADD CONSTRAINT "FK_64" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3670 (class 2606 OID 17122)
-- Name: category_urls FK_65; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category_urls
  ADD CONSTRAINT "FK_65" FOREIGN KEY (url_key, upper_key) REFERENCES public.url_keys(url_key, upper_key);


--
-- TOC entry 3671 (class 2606 OID 17127)
-- Name: category_urls FK_66; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category_urls
  ADD CONSTRAINT "FK_66" FOREIGN KEY (category_id) REFERENCES public.categories(category_id);


--
-- TOC entry 3666 (class 2606 OID 17132)
-- Name: categories FK_67; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
  ADD CONSTRAINT "FK_67" FOREIGN KEY (upper_category) REFERENCES public.categories(category_id);


--
-- TOC entry 3742 (class 2606 OID 17137)
-- Name: products_seo FK_68; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_seo
  ADD CONSTRAINT "FK_68" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3746 (class 2606 OID 17142)
-- Name: text_pages_seo FK_69; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_pages_seo
  ADD CONSTRAINT "FK_69" FOREIGN KEY (text_pages_id) REFERENCES public.text_pages(text_pages_id);


--
-- TOC entry 3741 (class 2606 OID 17147)
-- Name: products_pricing FK_7; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_pricing
  ADD CONSTRAINT "FK_7" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3744 (class 2606 OID 17152)
-- Name: text_page_urls FK_70; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_page_urls
  ADD CONSTRAINT "FK_70" FOREIGN KEY (url_key, upper_key) REFERENCES public.url_keys(url_key, upper_key);


--
-- TOC entry 3745 (class 2606 OID 17157)
-- Name: text_page_urls FK_71; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_page_urls
  ADD CONSTRAINT "FK_71" FOREIGN KEY (text_pages_id) REFERENCES public.text_pages(text_pages_id);


--
-- TOC entry 3665 (class 2606 OID 17162)
-- Name: blocks FK_72; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blocks
  ADD CONSTRAINT "FK_72" FOREIGN KEY (template_key) REFERENCES public.templates(template_key);


--
-- TOC entry 3662 (class 2606 OID 17167)
-- Name: block_id FK_73; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.block_id
  ADD CONSTRAINT "FK_73" FOREIGN KEY (block_id) REFERENCES public.backend_block(block_id) NOT VALID;


--
-- TOC entry 3663 (class 2606 OID 17172)
-- Name: block_id FK_74; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.block_id
  ADD CONSTRAINT "FK_74" FOREIGN KEY (category_id) REFERENCES public.categories(category_id) NOT VALID;


--
-- TOC entry 3664 (class 2606 OID 17177)
-- Name: block_id FK_75; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.block_id
  ADD CONSTRAINT "FK_75" FOREIGN KEY (product_id) REFERENCES public.products(product_id) NOT VALID;


--
-- TOC entry 3658 (class 2606 OID 17182)
-- Name: attribute_block FK_76; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attribute_block
  ADD CONSTRAINT "FK_76" FOREIGN KEY (block_id) REFERENCES public.backend_block(block_id);


--
-- TOC entry 3659 (class 2606 OID 17187)
-- Name: attribute_block FK_77; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attribute_block
  ADD CONSTRAINT "FK_77" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id);


--
-- TOC entry 3674 (class 2606 OID 17192)
-- Name: eav_attribute_bool FK_78; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_bool
  ADD CONSTRAINT "FK_78" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3675 (class 2606 OID 17197)
-- Name: eav_attribute_float FK_79; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_float
  ADD CONSTRAINT "FK_79" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3732 (class 2606 OID 17202)
-- Name: multi_select_attributes_int FK_8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_int
  ADD CONSTRAINT "FK_8" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3676 (class 2606 OID 17207)
-- Name: eav_attribute_int FK_80; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_int
  ADD CONSTRAINT "FK_80" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3677 (class 2606 OID 17212)
-- Name: eav_attribute_money FK_81; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_money
  ADD CONSTRAINT "FK_81" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3678 (class 2606 OID 17217)
-- Name: eav_attribute_multi_select FK_82; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_multi_select
  ADD CONSTRAINT "FK_82" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3679 (class 2606 OID 17222)
-- Name: eav_attribute_string FK_83; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_string
  ADD CONSTRAINT "FK_83" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3728 (class 2606 OID 17227)
-- Name: image_product FK_85; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.image_product
  ADD CONSTRAINT "FK_85" FOREIGN KEY (product_id) REFERENCES public.products(product_id) NOT VALID;


--
-- TOC entry 3729 (class 2606 OID 17232)
-- Name: image_product FK_86; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.image_product
  ADD CONSTRAINT "FK_86" FOREIGN KEY (image_url, image_name) REFERENCES public.image(image_url, image_name) NOT VALID;


--
-- TOC entry 3660 (class 2606 OID 17237)
-- Name: attribute_list FK_88; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attribute_list
  ADD CONSTRAINT "FK_88" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id);


--
-- TOC entry 3731 (class 2606 OID 17242)
-- Name: multi_select_attributes_float FK_9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_float
  ADD CONSTRAINT "FK_9" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3736 (class 2606 OID 17247)
-- Name: product_list FK_90; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_list
  ADD CONSTRAINT "FK_90" FOREIGN KEY (product_id) REFERENCES public.products(product_id) NOT VALID;


--
-- TOC entry 3737 (class 2606 OID 17252)
-- Name: product_list FK_91; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_list
  ADD CONSTRAINT "FK_91" FOREIGN KEY (list_item_id) REFERENCES public.list_items(list_item_id) NOT VALID;


--
-- TOC entry 3738 (class 2606 OID 17257)
-- Name: product_list FK_92; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_list
  ADD CONSTRAINT "FK_92" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3735 (class 2606 OID 17262)
-- Name: page_block FK_92; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.page_block
  ADD CONSTRAINT "FK_92" FOREIGN KEY (block_id) REFERENCES public.backend_block(block_id);


--
-- TOC entry 3962 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2025-04-07 14:57:49

--
-- PostgreSQL database dump complete
--

