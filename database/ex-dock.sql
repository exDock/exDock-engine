--
-- PostgreSQL database dump
--

-- Dumped from database version 14.17
-- Dumped by pg_dump version 16.1

-- Started on 2025-03-19 12:24:13

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
-- TOC entry 886 (class 1247 OID 16386)
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
-- TOC entry 889 (class 1247 OID 16396)
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
-- TOC entry 892 (class 1247 OID 16408)
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
-- TOC entry 895 (class 1247 OID 16418)
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
-- TOC entry 898 (class 1247 OID 16428)
-- Name: p_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.p_type AS ENUM (
  'product',
  'category',
  'text_page'
  );


ALTER TYPE public.p_type OWNER TO postgres;

--
-- TOC entry 273 (class 1255 OID 16435)
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
-- Name: backend_block; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.backend_block (
                                    block_id integer NOT NULL,
                                    block_name character varying(255),
                                    block_type character varying(255)
);


ALTER TABLE public.backend_block OWNER TO postgres;

--
-- TOC entry 211 (class 1259 OID 16444)
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
-- TOC entry 3905 (class 0 OID 0)
-- Dependencies: 211
-- Name: backend_block_block_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.backend_block_block_id_seq OWNED BY public.backend_block.block_id;


--
-- TOC entry 212 (class 1259 OID 16445)
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
-- TOC entry 213 (class 1259 OID 16448)
-- Name: block_attributes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.block_attributes (
                                       attribute_id character varying(100) NOT NULL,
                                       attribute_name character varying(255),
                                       attribute_type character varying(100)
);


ALTER TABLE public.block_attributes OWNER TO postgres;

--
-- TOC entry 214 (class 1259 OID 16451)
-- Name: block_id; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.block_id (
                               block_id integer NOT NULL,
                               category_id integer NOT NULL,
                               product_id integer NOT NULL
);


ALTER TABLE public.block_id OWNER TO postgres;

--
-- TOC entry 215 (class 1259 OID 16454)
-- Name: blocks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.blocks (
                             template_key character varying(100) NOT NULL
);


ALTER TABLE public.blocks OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 16457)
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
-- TOC entry 217 (class 1259 OID 16462)
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
-- TOC entry 3906 (class 0 OID 0)
-- Dependencies: 217
-- Name: categories_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.categories_category_id_seq OWNED BY public.categories.category_id;


--
-- TOC entry 218 (class 1259 OID 16463)
-- Name: categories_products; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categories_products (
                                          category_id integer NOT NULL,
                                          product_id integer NOT NULL
);


ALTER TABLE public.categories_products OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16466)
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
-- TOC entry 220 (class 1259 OID 16471)
-- Name: category_urls; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.category_urls (
                                    url_key character varying(100) NOT NULL,
                                    upper_key character varying(100) NOT NULL,
                                    category_id integer NOT NULL
);


ALTER TABLE public.category_urls OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16474)
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
-- TOC entry 222 (class 1259 OID 16477)
-- Name: eav; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav (
                          product_id integer NOT NULL,
                          attribute_key character varying(64) NOT NULL
);


ALTER TABLE public.eav OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16480)
-- Name: eav_attribute_bool; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_bool (
                                         attribute_id character varying(255) NOT NULL,
                                         attribute_key character varying(255) NOT NULL,
                                         value boolean
);


ALTER TABLE public.eav_attribute_bool OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 16485)
-- Name: eav_attribute_float; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_float (
                                          attribute_id character varying(255) NOT NULL,
                                          attribute_key character varying(255) NOT NULL,
                                          value double precision
);


ALTER TABLE public.eav_attribute_float OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16490)
-- Name: eav_attribute_int; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_int (
                                        attribute_id character varying(255) NOT NULL,
                                        attribute_key character varying(255) NOT NULL,
                                        value integer
);


ALTER TABLE public.eav_attribute_int OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 16495)
-- Name: eav_attribute_money; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_money (
                                          attribute_id character varying(255) NOT NULL,
                                          attribute_key character varying(255) NOT NULL,
                                          value numeric(11,2)
);


ALTER TABLE public.eav_attribute_money OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 16500)
-- Name: eav_attribute_multi_select; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_multi_select (
                                                 attribute_id character varying(255) NOT NULL,
                                                 attribute_key character varying(255) NOT NULL,
                                                 value integer
);


ALTER TABLE public.eav_attribute_multi_select OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 16505)
-- Name: eav_attribute_string; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_attribute_string (
                                           attribute_id character varying(255) NOT NULL,
                                           attribute_key character varying(255) NOT NULL,
                                           value character varying(255)
);


ALTER TABLE public.eav_attribute_string OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 16510)
-- Name: eav_global_bool; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_bool (
                                      product_id integer NOT NULL,
                                      attribute_key character varying(64) NOT NULL,
                                      value bit(1) NOT NULL
);


ALTER TABLE public.eav_global_bool OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 16513)
-- Name: eav_global_float; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_float (
                                       product_id integer NOT NULL,
                                       attribute_key character varying(64) NOT NULL,
                                       value double precision NOT NULL
);


ALTER TABLE public.eav_global_float OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 16516)
-- Name: eav_global_int; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_int (
                                     product_id integer NOT NULL,
                                     attribute_key character varying(64) NOT NULL,
                                     value integer NOT NULL
);


ALTER TABLE public.eav_global_int OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 16519)
-- Name: eav_global_money; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_money (
                                       product_id integer NOT NULL,
                                       attribute_key character varying(64) NOT NULL,
                                       value numeric(11,2) NOT NULL
);


ALTER TABLE public.eav_global_money OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 16522)
-- Name: eav_global_multi_select; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_multi_select (
                                              product_id integer NOT NULL,
                                              attribute_key character varying(64) NOT NULL,
                                              value integer NOT NULL
);


ALTER TABLE public.eav_global_multi_select OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 16525)
-- Name: eav_global_string; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eav_global_string (
                                        product_id integer NOT NULL,
                                        attribute_key character varying(64) NOT NULL,
                                        value text NOT NULL
);


ALTER TABLE public.eav_global_string OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 16530)
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
-- TOC entry 236 (class 1259 OID 16533)
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
-- TOC entry 237 (class 1259 OID 16536)
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
-- TOC entry 238 (class 1259 OID 16539)
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
-- TOC entry 239 (class 1259 OID 16542)
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
-- TOC entry 240 (class 1259 OID 16545)
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
-- TOC entry 241 (class 1259 OID 16550)
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
-- TOC entry 242 (class 1259 OID 16553)
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
-- TOC entry 243 (class 1259 OID 16556)
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
-- TOC entry 244 (class 1259 OID 16559)
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
-- TOC entry 245 (class 1259 OID 16562)
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
-- TOC entry 246 (class 1259 OID 16565)
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
-- TOC entry 271 (class 1259 OID 17192)
-- Name: image; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.image (
                            image_url character varying(255) NOT NULL,
                            image_name character varying(255) NOT NULL,
                            extensions character varying(255) NOT NULL
);


ALTER TABLE public.image OWNER TO postgres;

--
-- TOC entry 272 (class 1259 OID 17209)
-- Name: image_product; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.image_product (
                                    product_id integer NOT NULL,
                                    image_url character varying(255) NOT NULL
);


ALTER TABLE public.image_product OWNER TO postgres;

--
-- TOC entry 247 (class 1259 OID 16570)
-- Name: multi_select_attributes_bool; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.multi_select_attributes_bool (
                                                   attribute_key character varying(64) NOT NULL,
                                                   option integer NOT NULL,
                                                   value bit(1) NOT NULL
);


ALTER TABLE public.multi_select_attributes_bool OWNER TO postgres;

--
-- TOC entry 248 (class 1259 OID 16573)
-- Name: multi_select_attributes_float; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.multi_select_attributes_float (
                                                    attribute_key character varying(64) NOT NULL,
                                                    option integer NOT NULL,
                                                    value double precision NOT NULL
);


ALTER TABLE public.multi_select_attributes_float OWNER TO postgres;

--
-- TOC entry 249 (class 1259 OID 16576)
-- Name: multi_select_attributes_int; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.multi_select_attributes_int (
                                                  attribute_key character varying(64) NOT NULL,
                                                  option integer NOT NULL,
                                                  value integer NOT NULL
);


ALTER TABLE public.multi_select_attributes_int OWNER TO postgres;

--
-- TOC entry 250 (class 1259 OID 16579)
-- Name: multi_select_attributes_money; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.multi_select_attributes_money (
                                                    attribute_key character varying(64) NOT NULL,
                                                    option integer NOT NULL,
                                                    value numeric(11,2) NOT NULL
);


ALTER TABLE public.multi_select_attributes_money OWNER TO postgres;

--
-- TOC entry 251 (class 1259 OID 16582)
-- Name: multi_select_attributes_string; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.multi_select_attributes_string (
                                                     attribute_key character varying(64) NOT NULL,
                                                     option integer NOT NULL,
                                                     value text NOT NULL
);


ALTER TABLE public.multi_select_attributes_string OWNER TO postgres;

--
-- TOC entry 252 (class 1259 OID 16587)
-- Name: product_urls; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.product_urls (
                                   url_key character varying(100) NOT NULL,
                                   upper_key character varying(100) NOT NULL,
                                   product_id integer NOT NULL
);


ALTER TABLE public.product_urls OWNER TO postgres;

--
-- TOC entry 253 (class 1259 OID 16590)
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
                               manufacturer character varying(255) NOT NULL
);


ALTER TABLE public.products OWNER TO postgres;

--
-- TOC entry 254 (class 1259 OID 16595)
-- Name: products_pricing; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.products_pricing (
                                       product_id integer NOT NULL,
                                       price numeric(11,2) NOT NULL,
                                       sale_price numeric(11,2) NOT NULL,
                                       cost_price numeric(11,2) NOT NULL,
                                       tax_class character varying(100) NOT NULL
);


ALTER TABLE public.products_pricing OWNER TO postgres;

--
-- TOC entry 255 (class 1259 OID 16598)
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
-- TOC entry 3907 (class 0 OID 0)
-- Dependencies: 255
-- Name: products_product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.products_product_id_seq OWNED BY public.products.product_id;


--
-- TOC entry 256 (class 1259 OID 16599)
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
-- TOC entry 257 (class 1259 OID 16604)
-- Name: server_data; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.server_data (
                                  key character varying(45) NOT NULL,
                                  value text NOT NULL
);


ALTER TABLE public.server_data OWNER TO postgres;

--
-- TOC entry 258 (class 1259 OID 16609)
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
-- TOC entry 259 (class 1259 OID 16614)
-- Name: store_view; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.store_view (
                                 store_view_id integer NOT NULL,
                                 website_id integer NOT NULL,
                                 store_view_name character varying(255)
);


ALTER TABLE public.store_view OWNER TO postgres;

--
-- TOC entry 260 (class 1259 OID 16617)
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
-- TOC entry 3908 (class 0 OID 0)
-- Dependencies: 260
-- Name: store_view_store_view_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.store_view_store_view_id_seq OWNED BY public.store_view.store_view_id;


--
-- TOC entry 261 (class 1259 OID 16618)
-- Name: templates; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.templates (
                                template_key character varying(100) NOT NULL,
                                template_data text NOT NULL,
                                data_string text NOT NULL
);


ALTER TABLE public.templates OWNER TO postgres;

--
-- TOC entry 262 (class 1259 OID 16623)
-- Name: text_page_urls; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.text_page_urls (
                                     url_key character varying(100) NOT NULL,
                                     upper_key character varying(100) NOT NULL,
                                     text_pages_id integer NOT NULL
);


ALTER TABLE public.text_page_urls OWNER TO postgres;

--
-- TOC entry 263 (class 1259 OID 16626)
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
-- TOC entry 264 (class 1259 OID 16631)
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
-- TOC entry 265 (class 1259 OID 16636)
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
-- TOC entry 3909 (class 0 OID 0)
-- Dependencies: 265
-- Name: text_pages_text_pages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.text_pages_text_pages_id_seq OWNED BY public.text_pages.text_pages_id;


--
-- TOC entry 266 (class 1259 OID 16637)
-- Name: url_keys; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.url_keys (
                               url_key character varying(100) NOT NULL,
                               upper_key character varying(100) NOT NULL,
                               page_type public.p_type NOT NULL
);


ALTER TABLE public.url_keys OWNER TO postgres;

--
-- TOC entry 267 (class 1259 OID 16640)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
                            user_id integer NOT NULL,
                            email character varying(320) NOT NULL,
                            password character varying(100) NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 268 (class 1259 OID 16643)
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
-- TOC entry 3910 (class 0 OID 0)
-- Dependencies: 268
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- TOC entry 269 (class 1259 OID 16644)
-- Name: websites; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.websites (
                               website_id integer NOT NULL,
                               website_name character varying(255)
);


ALTER TABLE public.websites OWNER TO postgres;

--
-- TOC entry 270 (class 1259 OID 16647)
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
-- TOC entry 3911 (class 0 OID 0)
-- Dependencies: 270
-- Name: websites_website_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.websites_website_id_seq OWNED BY public.websites.website_id;


--
-- TOC entry 3479 (class 2604 OID 16648)
-- Name: backend_block block_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.backend_block ALTER COLUMN block_id SET DEFAULT nextval('public.backend_block_block_id_seq'::regclass);


--
-- TOC entry 3480 (class 2604 OID 16649)
-- Name: categories category_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories ALTER COLUMN category_id SET DEFAULT nextval('public.categories_category_id_seq'::regclass);


--
-- TOC entry 3481 (class 2604 OID 16650)
-- Name: products product_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products ALTER COLUMN product_id SET DEFAULT nextval('public.products_product_id_seq'::regclass);


--
-- TOC entry 3482 (class 2604 OID 16651)
-- Name: store_view store_view_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.store_view ALTER COLUMN store_view_id SET DEFAULT nextval('public.store_view_store_view_id_seq'::regclass);


--
-- TOC entry 3483 (class 2604 OID 16652)
-- Name: text_pages text_pages_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_pages ALTER COLUMN text_pages_id SET DEFAULT nextval('public.text_pages_text_pages_id_seq'::regclass);


--
-- TOC entry 3484 (class 2604 OID 16653)
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- TOC entry 3485 (class 2604 OID 16654)
-- Name: websites website_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.websites ALTER COLUMN website_id SET DEFAULT nextval('public.websites_website_id_seq'::regclass);


--
-- TOC entry 3835 (class 0 OID 16436)
-- Dependencies: 209
-- Data for Name: attribute_block; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.attribute_block (block_id, attribute_id) FROM stdin;
1	sku
1	location
1	ean
1	manufacturer
2	description
2	short_description
3	price
3	sale_price
\.


--
-- TOC entry 3836 (class 0 OID 16439)
-- Dependencies: 210
-- Data for Name: backend_block; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.backend_block (block_id, block_name, block_type) FROM stdin;
1	Id data	id_information
2	Content	standard
3	Price	product_price
\.


--
-- TOC entry 3838 (class 0 OID 16445)
-- Dependencies: 212
-- Data for Name: backend_permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.backend_permissions (user_id, user_permissions, server_settings, template, category_content, category_products, product_content, product_price, product_warehouse, text_pages, "API_KEY") FROM stdin;
1	read-write	read-write	read-write	read-write	read-write	read-write	read-write	read-write	read-write	\N
\.


--
-- TOC entry 3839 (class 0 OID 16448)
-- Dependencies: 213
-- Data for Name: block_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.block_attributes (attribute_id, attribute_name, attribute_type) FROM stdin;
sku	SKU	text
location	Location	text
ean	EAN	text
manufacturer	Manufacturer	text
description	Description	wysiwyg
short_description	Short description	wysiwyg
cost_price	Cost price	price
tax_class	Tax class	text
price	Price	price
sale_price	Sale price	price
\.


--
-- TOC entry 3840 (class 0 OID 16451)
-- Dependencies: 214
-- Data for Name: block_id; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.block_id (block_id, category_id, product_id) FROM stdin;
\.


--
-- TOC entry 3841 (class 0 OID 16454)
-- Dependencies: 215
-- Data for Name: blocks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.blocks (template_key) FROM stdin;
\.


--
-- TOC entry 3842 (class 0 OID 16457)
-- Dependencies: 216
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.categories (category_id, upper_category, name, short_description, description) FROM stdin;
\.


--
-- TOC entry 3844 (class 0 OID 16463)
-- Dependencies: 218
-- Data for Name: categories_products; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.categories_products (category_id, product_id) FROM stdin;
\.


--
-- TOC entry 3845 (class 0 OID 16466)
-- Dependencies: 219
-- Data for Name: categories_seo; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.categories_seo (category_id, meta_title, meta_description, meta_keywords, page_index) FROM stdin;
\.


--
-- TOC entry 3846 (class 0 OID 16471)
-- Dependencies: 220
-- Data for Name: category_urls; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.category_urls (url_key, upper_key, category_id) FROM stdin;
\.


--
-- TOC entry 3847 (class 0 OID 16474)
-- Dependencies: 221
-- Data for Name: custom_product_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.custom_product_attributes (attribute_key, scope, name, type, multiselect, required) FROM stdin;
\.


--
-- TOC entry 3848 (class 0 OID 16477)
-- Dependencies: 222
-- Data for Name: eav; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav (product_id, attribute_key) FROM stdin;
\.


--
-- TOC entry 3849 (class 0 OID 16480)
-- Dependencies: 223
-- Data for Name: eav_attribute_bool; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_attribute_bool (attribute_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3850 (class 0 OID 16485)
-- Dependencies: 224
-- Data for Name: eav_attribute_float; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_attribute_float (attribute_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3851 (class 0 OID 16490)
-- Dependencies: 225
-- Data for Name: eav_attribute_int; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_attribute_int (attribute_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3852 (class 0 OID 16495)
-- Dependencies: 226
-- Data for Name: eav_attribute_money; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_attribute_money (attribute_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3853 (class 0 OID 16500)
-- Dependencies: 227
-- Data for Name: eav_attribute_multi_select; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_attribute_multi_select (attribute_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3854 (class 0 OID 16505)
-- Dependencies: 228
-- Data for Name: eav_attribute_string; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_attribute_string (attribute_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3855 (class 0 OID 16510)
-- Dependencies: 229
-- Data for Name: eav_global_bool; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_global_bool (product_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3856 (class 0 OID 16513)
-- Dependencies: 230
-- Data for Name: eav_global_float; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_global_float (product_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3857 (class 0 OID 16516)
-- Dependencies: 231
-- Data for Name: eav_global_int; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_global_int (product_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3858 (class 0 OID 16519)
-- Dependencies: 232
-- Data for Name: eav_global_money; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_global_money (product_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3859 (class 0 OID 16522)
-- Dependencies: 233
-- Data for Name: eav_global_multi_select; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_global_multi_select (product_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3860 (class 0 OID 16525)
-- Dependencies: 234
-- Data for Name: eav_global_string; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_global_string (product_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3861 (class 0 OID 16530)
-- Dependencies: 235
-- Data for Name: eav_store_view_bool; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_store_view_bool (product_id, store_view_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3862 (class 0 OID 16533)
-- Dependencies: 236
-- Data for Name: eav_store_view_float; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_store_view_float (product_id, store_view_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3863 (class 0 OID 16536)
-- Dependencies: 237
-- Data for Name: eav_store_view_int; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_store_view_int (product_id, store_view_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3864 (class 0 OID 16539)
-- Dependencies: 238
-- Data for Name: eav_store_view_money; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_store_view_money (product_id, store_view_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3865 (class 0 OID 16542)
-- Dependencies: 239
-- Data for Name: eav_store_view_multi_select; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_store_view_multi_select (product_id, store_view_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3866 (class 0 OID 16545)
-- Dependencies: 240
-- Data for Name: eav_store_view_string; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_store_view_string (product_id, store_view_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3867 (class 0 OID 16550)
-- Dependencies: 241
-- Data for Name: eav_website_bool; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_website_bool (product_id, website_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3868 (class 0 OID 16553)
-- Dependencies: 242
-- Data for Name: eav_website_float; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_website_float (product_id, website_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3869 (class 0 OID 16556)
-- Dependencies: 243
-- Data for Name: eav_website_int; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_website_int (product_id, website_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3870 (class 0 OID 16559)
-- Dependencies: 244
-- Data for Name: eav_website_money; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_website_money (product_id, website_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3871 (class 0 OID 16562)
-- Dependencies: 245
-- Data for Name: eav_website_multi_select; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_website_multi_select (product_id, website_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3872 (class 0 OID 16565)
-- Dependencies: 246
-- Data for Name: eav_website_string; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eav_website_string (product_id, website_id, attribute_key, value) FROM stdin;
\.


--
-- TOC entry 3897 (class 0 OID 17192)
-- Dependencies: 271
-- Data for Name: image; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.image (image_url, image_name, extensions) FROM stdin;
\.


--
-- TOC entry 3898 (class 0 OID 17209)
-- Dependencies: 272
-- Data for Name: image_product; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.image_product (product_id, image_url) FROM stdin;
\.


--
-- TOC entry 3873 (class 0 OID 16570)
-- Dependencies: 247
-- Data for Name: multi_select_attributes_bool; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.multi_select_attributes_bool (attribute_key, option, value) FROM stdin;
\.


--
-- TOC entry 3874 (class 0 OID 16573)
-- Dependencies: 248
-- Data for Name: multi_select_attributes_float; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.multi_select_attributes_float (attribute_key, option, value) FROM stdin;
\.


--
-- TOC entry 3875 (class 0 OID 16576)
-- Dependencies: 249
-- Data for Name: multi_select_attributes_int; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.multi_select_attributes_int (attribute_key, option, value) FROM stdin;
\.


--
-- TOC entry 3876 (class 0 OID 16579)
-- Dependencies: 250
-- Data for Name: multi_select_attributes_money; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.multi_select_attributes_money (attribute_key, option, value) FROM stdin;
\.


--
-- TOC entry 3877 (class 0 OID 16582)
-- Dependencies: 251
-- Data for Name: multi_select_attributes_string; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.multi_select_attributes_string (attribute_key, option, value) FROM stdin;
\.


--
-- TOC entry 3878 (class 0 OID 16587)
-- Dependencies: 252
-- Data for Name: product_urls; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.product_urls (url_key, upper_key, product_id) FROM stdin;
\.


--
-- TOC entry 3879 (class 0 OID 16590)
-- Dependencies: 253
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.products (product_id, name, short_name, description, short_description, sku, ean, manufacturer) FROM stdin;
\.


--
-- TOC entry 3880 (class 0 OID 16595)
-- Dependencies: 254
-- Data for Name: products_pricing; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.products_pricing (product_id, price, sale_price, cost_price, tax_class) FROM stdin;
\.


--
-- TOC entry 3882 (class 0 OID 16599)
-- Dependencies: 256
-- Data for Name: products_seo; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.products_seo (product_id, meta_title, meta_description, meta_keywords, page_index) FROM stdin;
\.


--
-- TOC entry 3883 (class 0 OID 16604)
-- Dependencies: 257
-- Data for Name: server_data; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.server_data (key, value) FROM stdin;
\.


--
-- TOC entry 3884 (class 0 OID 16609)
-- Dependencies: 258
-- Data for Name: server_version; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.server_version (major, minor, patch, version_name, version_description) FROM stdin;
\.


--
-- TOC entry 3885 (class 0 OID 16614)
-- Dependencies: 259
-- Data for Name: store_view; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.store_view (store_view_id, website_id, store_view_name) FROM stdin;
\.


--
-- TOC entry 3887 (class 0 OID 16618)
-- Dependencies: 261
-- Data for Name: templates; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.templates (template_key, template_data, data_string) FROM stdin;
testKey	<test>testData</test>
\.


--
-- TOC entry 3888 (class 0 OID 16623)
-- Dependencies: 262
-- Data for Name: text_page_urls; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.text_page_urls (url_key, upper_key, text_pages_id) FROM stdin;
\.


--
-- TOC entry 3889 (class 0 OID 16626)
-- Dependencies: 263
-- Data for Name: text_pages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.text_pages (text_pages_id, name, short_text, text) FROM stdin;
\.


--
-- TOC entry 3890 (class 0 OID 16631)
-- Dependencies: 264
-- Data for Name: text_pages_seo; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.text_pages_seo (text_pages_id, meta_title, meta_description, meta_keywords, page_index) FROM stdin;
\.


--
-- TOC entry 3892 (class 0 OID 16637)
-- Dependencies: 266
-- Data for Name: url_keys; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.url_keys (url_key, upper_key, page_type) FROM stdin;
\.


--
-- TOC entry 3893 (class 0 OID 16640)
-- Dependencies: 267
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, email, password) FROM stdin;
1	test@test.com	$2a$12$wHeihDNoufF.9UHXKctSC.wVP1XsraVMz0A1P/qNJnYjyfbVgJIvq
\.


--
-- TOC entry 3895 (class 0 OID 16644)
-- Dependencies: 269
-- Data for Name: websites; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.websites (website_id, website_name) FROM stdin;
\.


--
-- TOC entry 3912 (class 0 OID 0)
-- Dependencies: 211
-- Name: backend_block_block_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.backend_block_block_id_seq', 3, true);


--
-- TOC entry 3913 (class 0 OID 0)
-- Dependencies: 217
-- Name: categories_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.categories_category_id_seq', 1, false);


--
-- TOC entry 3914 (class 0 OID 0)
-- Dependencies: 255
-- Name: products_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.products_product_id_seq', 1, false);


--
-- TOC entry 3915 (class 0 OID 0)
-- Dependencies: 260
-- Name: store_view_store_view_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.store_view_store_view_id_seq', 1, false);


--
-- TOC entry 3916 (class 0 OID 0)
-- Dependencies: 265
-- Name: text_pages_text_pages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.text_pages_text_pages_id_seq', 1, false);


--
-- TOC entry 3917 (class 0 OID 0)
-- Dependencies: 268
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 1, true);


--
-- TOC entry 3918 (class 0 OID 0)
-- Dependencies: 270
-- Name: websites_website_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.websites_website_id_seq', 1, false);


--
-- TOC entry 3596 (class 2606 OID 16656)
-- Name: url_keys UK_1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.url_keys
  ADD CONSTRAINT "UK_1" UNIQUE (url_key);


--
-- TOC entry 3487 (class 2606 OID 16658)
-- Name: attribute_block attribute_block_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attribute_block
  ADD CONSTRAINT attribute_block_pkey PRIMARY KEY (block_id, attribute_id);


--
-- TOC entry 3489 (class 2606 OID 16660)
-- Name: backend_block backend_block_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.backend_block
  ADD CONSTRAINT backend_block_pkey PRIMARY KEY (block_id);


--
-- TOC entry 3492 (class 2606 OID 16662)
-- Name: block_attributes block_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.block_attributes
  ADD CONSTRAINT block_attributes_pkey PRIMARY KEY (attribute_id);


--
-- TOC entry 3494 (class 2606 OID 16664)
-- Name: block_id block_id_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.block_id
  ADD CONSTRAINT block_id_pkey PRIMARY KEY (block_id, category_id, product_id);


--
-- TOC entry 3496 (class 2606 OID 16666)
-- Name: blocks blocks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blocks
  ADD CONSTRAINT blocks_pkey PRIMARY KEY (template_key);


--
-- TOC entry 3499 (class 2606 OID 16668)
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
  ADD CONSTRAINT categories_pkey PRIMARY KEY (category_id);


--
-- TOC entry 3502 (class 2606 OID 16670)
-- Name: categories_products categories_products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories_products
  ADD CONSTRAINT categories_products_pkey PRIMARY KEY (category_id, product_id);


--
-- TOC entry 3504 (class 2606 OID 16672)
-- Name: categories_seo categories_seo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories_seo
  ADD CONSTRAINT categories_seo_pkey PRIMARY KEY (category_id);


--
-- TOC entry 3507 (class 2606 OID 16674)
-- Name: category_urls category_urls_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category_urls
  ADD CONSTRAINT category_urls_pkey PRIMARY KEY (url_key, upper_key);


--
-- TOC entry 3509 (class 2606 OID 16676)
-- Name: custom_product_attributes custom_product_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_product_attributes
  ADD CONSTRAINT custom_product_attributes_pkey PRIMARY KEY (attribute_key);


--
-- TOC entry 3514 (class 2606 OID 16678)
-- Name: eav_attribute_bool eav_attribute_bool_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_bool
  ADD CONSTRAINT eav_attribute_bool_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3516 (class 2606 OID 16680)
-- Name: eav_attribute_float eav_attribute_float_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_float
  ADD CONSTRAINT eav_attribute_float_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3518 (class 2606 OID 16682)
-- Name: eav_attribute_int eav_attribute_int_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_int
  ADD CONSTRAINT eav_attribute_int_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3520 (class 2606 OID 16684)
-- Name: eav_attribute_money eav_attribute_money_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_money
  ADD CONSTRAINT eav_attribute_money_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3522 (class 2606 OID 16686)
-- Name: eav_attribute_multi_select eav_attribute_multi_select_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_multi_select
  ADD CONSTRAINT eav_attribute_multi_select_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3524 (class 2606 OID 16688)
-- Name: eav_attribute_string eav_attribute_string_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_string
  ADD CONSTRAINT eav_attribute_string_pkey PRIMARY KEY (attribute_id, attribute_key);


--
-- TOC entry 3526 (class 2606 OID 16690)
-- Name: eav_global_bool eav_global_bool_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_bool
  ADD CONSTRAINT eav_global_bool_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3528 (class 2606 OID 16692)
-- Name: eav_global_float eav_global_float_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_float
  ADD CONSTRAINT eav_global_float_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3530 (class 2606 OID 16694)
-- Name: eav_global_int eav_global_int_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_int
  ADD CONSTRAINT eav_global_int_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3532 (class 2606 OID 16696)
-- Name: eav_global_money eav_global_money_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_money
  ADD CONSTRAINT eav_global_money_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3534 (class 2606 OID 16698)
-- Name: eav_global_multi_select eav_global_multi_select_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_multi_select
  ADD CONSTRAINT eav_global_multi_select_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3536 (class 2606 OID 16700)
-- Name: eav_global_string eav_global_string_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_string
  ADD CONSTRAINT eav_global_string_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3511 (class 2606 OID 16702)
-- Name: eav eav_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav
  ADD CONSTRAINT eav_pkey PRIMARY KEY (product_id, attribute_key);


--
-- TOC entry 3538 (class 2606 OID 16704)
-- Name: eav_store_view_bool eav_store_view_bool_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_bool
  ADD CONSTRAINT eav_store_view_bool_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3541 (class 2606 OID 16706)
-- Name: eav_store_view_float eav_store_view_float_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_float
  ADD CONSTRAINT eav_store_view_float_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3543 (class 2606 OID 16708)
-- Name: eav_store_view_int eav_store_view_int_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_int
  ADD CONSTRAINT eav_store_view_int_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3545 (class 2606 OID 16710)
-- Name: eav_store_view_money eav_store_view_money_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_money
  ADD CONSTRAINT eav_store_view_money_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3547 (class 2606 OID 16712)
-- Name: eav_store_view_multi_select eav_store_view_multi_select_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_multi_select
  ADD CONSTRAINT eav_store_view_multi_select_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3549 (class 2606 OID 16714)
-- Name: eav_store_view_string eav_store_view_string_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_string
  ADD CONSTRAINT eav_store_view_string_pkey PRIMARY KEY (product_id, store_view_id, attribute_key);


--
-- TOC entry 3551 (class 2606 OID 16716)
-- Name: eav_website_bool eav_website_bool_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_bool
  ADD CONSTRAINT eav_website_bool_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3553 (class 2606 OID 16718)
-- Name: eav_website_float eav_website_float_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_float
  ADD CONSTRAINT eav_website_float_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3555 (class 2606 OID 16720)
-- Name: eav_website_int eav_website_int_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_int
  ADD CONSTRAINT eav_website_int_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3557 (class 2606 OID 16722)
-- Name: eav_website_money eav_website_money_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_money
  ADD CONSTRAINT eav_website_money_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3559 (class 2606 OID 16724)
-- Name: eav_website_multi_select eav_website_multi_select_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_multi_select
  ADD CONSTRAINT eav_website_multi_select_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3561 (class 2606 OID 16726)
-- Name: eav_website_string eav_website_string_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_string
  ADD CONSTRAINT eav_website_string_pkey PRIMARY KEY (product_id, website_id, attribute_key);


--
-- TOC entry 3607 (class 2606 OID 17198)
-- Name: image image_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.image
  ADD CONSTRAINT image_pkey PRIMARY KEY (image_url);


--
-- TOC entry 3609 (class 2606 OID 17213)
-- Name: image_product image_product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.image_product
  ADD CONSTRAINT image_product_pkey PRIMARY KEY (product_id, image_url);


--
-- TOC entry 3563 (class 2606 OID 16728)
-- Name: multi_select_attributes_bool multi_select_attributes_bool_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_bool
  ADD CONSTRAINT multi_select_attributes_bool_pkey PRIMARY KEY (attribute_key, option);


--
-- TOC entry 3565 (class 2606 OID 16730)
-- Name: multi_select_attributes_float multi_select_attributes_float_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_float
  ADD CONSTRAINT multi_select_attributes_float_pkey PRIMARY KEY (attribute_key, option);


--
-- TOC entry 3567 (class 2606 OID 16732)
-- Name: multi_select_attributes_int multi_select_attributes_int_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_int
  ADD CONSTRAINT multi_select_attributes_int_pkey PRIMARY KEY (attribute_key, option);


--
-- TOC entry 3569 (class 2606 OID 16734)
-- Name: multi_select_attributes_money multi_select_attributes_money_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_money
  ADD CONSTRAINT multi_select_attributes_money_pkey PRIMARY KEY (attribute_key, option);


--
-- TOC entry 3571 (class 2606 OID 16736)
-- Name: multi_select_attributes_string multi_select_attributes_string_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_string
  ADD CONSTRAINT multi_select_attributes_string_pkey PRIMARY KEY (attribute_key, option);


--
-- TOC entry 3573 (class 2606 OID 16738)
-- Name: product_urls product_urls_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_urls
  ADD CONSTRAINT product_urls_pkey PRIMARY KEY (url_key, upper_key);


--
-- TOC entry 3575 (class 2606 OID 16740)
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
  ADD CONSTRAINT products_pkey PRIMARY KEY (product_id);


--
-- TOC entry 3577 (class 2606 OID 16742)
-- Name: products_pricing products_pricing_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_pricing
  ADD CONSTRAINT products_pricing_pkey PRIMARY KEY (product_id);


--
-- TOC entry 3579 (class 2606 OID 16744)
-- Name: products_seo products_seo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_seo
  ADD CONSTRAINT products_seo_pkey PRIMARY KEY (product_id);


--
-- TOC entry 3581 (class 2606 OID 16746)
-- Name: server_data server_data_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_data
  ADD CONSTRAINT server_data_pkey PRIMARY KEY (key);


--
-- TOC entry 3583 (class 2606 OID 16748)
-- Name: server_version server_version_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_version
  ADD CONSTRAINT server_version_pkey PRIMARY KEY (major, minor, patch);


--
-- TOC entry 3585 (class 2606 OID 16750)
-- Name: store_view store_view_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.store_view
  ADD CONSTRAINT store_view_pkey PRIMARY KEY (store_view_id);


--
-- TOC entry 3588 (class 2606 OID 16752)
-- Name: templates templates_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.templates
  ADD CONSTRAINT templates_pkey PRIMARY KEY (template_key);


--
-- TOC entry 3590 (class 2606 OID 16754)
-- Name: text_page_urls text_page_urls_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_page_urls
  ADD CONSTRAINT text_page_urls_pkey PRIMARY KEY (url_key, upper_key);


--
-- TOC entry 3592 (class 2606 OID 16756)
-- Name: text_pages text_pages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_pages
  ADD CONSTRAINT text_pages_pkey PRIMARY KEY (text_pages_id);


--
-- TOC entry 3594 (class 2606 OID 16758)
-- Name: text_pages_seo text_pages_seo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_pages_seo
  ADD CONSTRAINT text_pages_seo_pkey PRIMARY KEY (text_pages_id);


--
-- TOC entry 3599 (class 2606 OID 16760)
-- Name: url_keys url_keys_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.url_keys
  ADD CONSTRAINT url_keys_pkey PRIMARY KEY (url_key, upper_key);


--
-- TOC entry 3601 (class 2606 OID 16762)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
  ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 3603 (class 2606 OID 16764)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
  ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3605 (class 2606 OID 16766)
-- Name: websites websites_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.websites
  ADD CONSTRAINT websites_pkey PRIMARY KEY (website_id);


--
-- TOC entry 3490 (class 1259 OID 16767)
-- Name: fki_FK_1; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_1" ON public.backend_permissions USING btree (user_id);


--
-- TOC entry 3512 (class 1259 OID 16768)
-- Name: fki_FK_2; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_2" ON public.eav USING btree (attribute_key);


--
-- TOC entry 3539 (class 1259 OID 16769)
-- Name: fki_FK_3; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_3" ON public.eav_store_view_bool USING btree (attribute_key);


--
-- TOC entry 3505 (class 1259 OID 16770)
-- Name: fki_FK_61; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_61" ON public.categories_seo USING btree (category_id);


--
-- TOC entry 3597 (class 1259 OID 16771)
-- Name: fki_FK_62; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_62" ON public.url_keys USING btree (upper_key);


--
-- TOC entry 3500 (class 1259 OID 16772)
-- Name: fki_FK_67; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_67" ON public.categories USING btree (upper_category);


--
-- TOC entry 3586 (class 1259 OID 16773)
-- Name: fki_FK_72; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_72" ON public.templates USING btree (template_key);


--
-- TOC entry 3497 (class 1259 OID 16774)
-- Name: fki_FK_73; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "fki_FK_73" ON public.blocks USING btree (template_key);


--
-- TOC entry 3695 (class 2620 OID 16775)
-- Name: url_keys after_insert_url_keys; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER after_insert_url_keys AFTER INSERT ON public.url_keys FOR EACH ROW EXECUTE FUNCTION public.check_root_url();


--
-- TOC entry 3612 (class 2606 OID 16776)
-- Name: backend_permissions FK_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.backend_permissions
  ADD CONSTRAINT "FK_1" FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 3682 (class 2606 OID 16781)
-- Name: multi_select_attributes_money FK_10; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_money
  ADD CONSTRAINT "FK_10" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3679 (class 2606 OID 16786)
-- Name: multi_select_attributes_bool FK_11; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_bool
  ADD CONSTRAINT "FK_11" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3641 (class 2606 OID 16791)
-- Name: eav_global_string FK_12; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_string
  ADD CONSTRAINT "FK_12" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3642 (class 2606 OID 16796)
-- Name: eav_global_string FK_13; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_string
  ADD CONSTRAINT "FK_13" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3635 (class 2606 OID 16801)
-- Name: eav_global_int FK_14; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_int
  ADD CONSTRAINT "FK_14" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3636 (class 2606 OID 16806)
-- Name: eav_global_int FK_15; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_int
  ADD CONSTRAINT "FK_15" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3633 (class 2606 OID 16811)
-- Name: eav_global_float FK_16; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_float
  ADD CONSTRAINT "FK_16" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3634 (class 2606 OID 16816)
-- Name: eav_global_float FK_17; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_float
  ADD CONSTRAINT "FK_17" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3637 (class 2606 OID 16821)
-- Name: eav_global_money FK_18; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_money
  ADD CONSTRAINT "FK_18" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3638 (class 2606 OID 16826)
-- Name: eav_global_money FK_19; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_money
  ADD CONSTRAINT "FK_19" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3623 (class 2606 OID 16831)
-- Name: eav FK_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav
  ADD CONSTRAINT "FK_2" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3631 (class 2606 OID 16836)
-- Name: eav_global_bool FK_20; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_bool
  ADD CONSTRAINT "FK_20" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3632 (class 2606 OID 16841)
-- Name: eav_global_bool FK_21; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_bool
  ADD CONSTRAINT "FK_21" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3639 (class 2606 OID 16846)
-- Name: eav_global_multi_select FK_22; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_multi_select
  ADD CONSTRAINT "FK_22" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3688 (class 2606 OID 16851)
-- Name: store_view FK_22_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.store_view
  ADD CONSTRAINT "FK_22_1" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3640 (class 2606 OID 16856)
-- Name: eav_global_multi_select FK_23; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_global_multi_select
  ADD CONSTRAINT "FK_23" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3661 (class 2606 OID 16861)
-- Name: eav_website_bool FK_23_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_bool
  ADD CONSTRAINT "FK_23_1" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3662 (class 2606 OID 16866)
-- Name: eav_website_bool FK_24; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_bool
  ADD CONSTRAINT "FK_24" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3663 (class 2606 OID 16871)
-- Name: eav_website_bool FK_25; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_bool
  ADD CONSTRAINT "FK_25" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3664 (class 2606 OID 16876)
-- Name: eav_website_float FK_26; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_float
  ADD CONSTRAINT "FK_26" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3665 (class 2606 OID 16881)
-- Name: eav_website_float FK_27; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_float
  ADD CONSTRAINT "FK_27" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3666 (class 2606 OID 16886)
-- Name: eav_website_float FK_28; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_float
  ADD CONSTRAINT "FK_28" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3667 (class 2606 OID 16891)
-- Name: eav_website_int FK_29; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_int
  ADD CONSTRAINT "FK_29" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3624 (class 2606 OID 16896)
-- Name: eav FK_3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav
  ADD CONSTRAINT "FK_3" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3668 (class 2606 OID 16901)
-- Name: eav_website_int FK_30; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_int
  ADD CONSTRAINT "FK_30" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3669 (class 2606 OID 16906)
-- Name: eav_website_int FK_31; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_int
  ADD CONSTRAINT "FK_31" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3670 (class 2606 OID 16911)
-- Name: eav_website_money FK_32; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_money
  ADD CONSTRAINT "FK_32" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3671 (class 2606 OID 16916)
-- Name: eav_website_money FK_33; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_money
  ADD CONSTRAINT "FK_33" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3672 (class 2606 OID 16921)
-- Name: eav_website_money FK_34; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_money
  ADD CONSTRAINT "FK_34" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3673 (class 2606 OID 16926)
-- Name: eav_website_multi_select FK_35; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_multi_select
  ADD CONSTRAINT "FK_35" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3674 (class 2606 OID 16931)
-- Name: eav_website_multi_select FK_36; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_multi_select
  ADD CONSTRAINT "FK_36" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3675 (class 2606 OID 16936)
-- Name: eav_website_multi_select FK_37; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_multi_select
  ADD CONSTRAINT "FK_37" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3676 (class 2606 OID 16941)
-- Name: eav_website_string FK_38; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_string
  ADD CONSTRAINT "FK_38" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3677 (class 2606 OID 16946)
-- Name: eav_website_string FK_39; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_string
  ADD CONSTRAINT "FK_39" FOREIGN KEY (website_id) REFERENCES public.websites(website_id);


--
-- TOC entry 3678 (class 2606 OID 16951)
-- Name: eav_website_string FK_40; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_website_string
  ADD CONSTRAINT "FK_40" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3643 (class 2606 OID 16956)
-- Name: eav_store_view_bool FK_41; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_bool
  ADD CONSTRAINT "FK_41" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3644 (class 2606 OID 16961)
-- Name: eav_store_view_bool FK_42; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_bool
  ADD CONSTRAINT "FK_42" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3645 (class 2606 OID 16966)
-- Name: eav_store_view_bool FK_43; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_bool
  ADD CONSTRAINT "FK_43" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3646 (class 2606 OID 16971)
-- Name: eav_store_view_float FK_44; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_float
  ADD CONSTRAINT "FK_44" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3647 (class 2606 OID 16976)
-- Name: eav_store_view_float FK_45; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_float
  ADD CONSTRAINT "FK_45" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3648 (class 2606 OID 16981)
-- Name: eav_store_view_float FK_46; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_float
  ADD CONSTRAINT "FK_46" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3649 (class 2606 OID 16986)
-- Name: eav_store_view_int FK_47; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_int
  ADD CONSTRAINT "FK_47" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3650 (class 2606 OID 16991)
-- Name: eav_store_view_int FK_48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_int
  ADD CONSTRAINT "FK_48" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3651 (class 2606 OID 16996)
-- Name: eav_store_view_int FK_49; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_int
  ADD CONSTRAINT "FK_49" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3652 (class 2606 OID 17001)
-- Name: eav_store_view_money FK_50; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_money
  ADD CONSTRAINT "FK_50" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3653 (class 2606 OID 17006)
-- Name: eav_store_view_money FK_51; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_money
  ADD CONSTRAINT "FK_51" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3654 (class 2606 OID 17011)
-- Name: eav_store_view_money FK_52; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_money
  ADD CONSTRAINT "FK_52" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3655 (class 2606 OID 17016)
-- Name: eav_store_view_multi_select FK_53; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_multi_select
  ADD CONSTRAINT "FK_53" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3656 (class 2606 OID 17021)
-- Name: eav_store_view_multi_select FK_54; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_multi_select
  ADD CONSTRAINT "FK_54" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3657 (class 2606 OID 17026)
-- Name: eav_store_view_multi_select FK_55; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_multi_select
  ADD CONSTRAINT "FK_55" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3658 (class 2606 OID 17031)
-- Name: eav_store_view_string FK_56; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_string
  ADD CONSTRAINT "FK_56" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3659 (class 2606 OID 17036)
-- Name: eav_store_view_string FK_57; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_string
  ADD CONSTRAINT "FK_57" FOREIGN KEY (store_view_id) REFERENCES public.store_view(store_view_id);


--
-- TOC entry 3660 (class 2606 OID 17041)
-- Name: eav_store_view_string FK_58; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_store_view_string
  ADD CONSTRAINT "FK_58" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3618 (class 2606 OID 17046)
-- Name: categories_products FK_59; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories_products
  ADD CONSTRAINT "FK_59" FOREIGN KEY (category_id) REFERENCES public.categories(category_id);


--
-- TOC entry 3683 (class 2606 OID 17051)
-- Name: multi_select_attributes_string FK_6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_string
  ADD CONSTRAINT "FK_6" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3619 (class 2606 OID 17056)
-- Name: categories_products FK_60; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories_products
  ADD CONSTRAINT "FK_60" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3620 (class 2606 OID 17061)
-- Name: categories_seo FK_61; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories_seo
  ADD CONSTRAINT "FK_61" FOREIGN KEY (category_id) REFERENCES public.categories(category_id);


--
-- TOC entry 3692 (class 2606 OID 17066)
-- Name: url_keys FK_62; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.url_keys
  ADD CONSTRAINT "FK_62" FOREIGN KEY (upper_key) REFERENCES public.url_keys(url_key);


--
-- TOC entry 3684 (class 2606 OID 17071)
-- Name: product_urls FK_63; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_urls
  ADD CONSTRAINT "FK_63" FOREIGN KEY (url_key, upper_key) REFERENCES public.url_keys(url_key, upper_key);


--
-- TOC entry 3685 (class 2606 OID 17076)
-- Name: product_urls FK_64; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_urls
  ADD CONSTRAINT "FK_64" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3621 (class 2606 OID 17081)
-- Name: category_urls FK_65; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category_urls
  ADD CONSTRAINT "FK_65" FOREIGN KEY (url_key, upper_key) REFERENCES public.url_keys(url_key, upper_key);


--
-- TOC entry 3622 (class 2606 OID 17086)
-- Name: category_urls FK_66; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category_urls
  ADD CONSTRAINT "FK_66" FOREIGN KEY (category_id) REFERENCES public.categories(category_id);


--
-- TOC entry 3617 (class 2606 OID 17091)
-- Name: categories FK_67; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
  ADD CONSTRAINT "FK_67" FOREIGN KEY (upper_category) REFERENCES public.categories(category_id);


--
-- TOC entry 3687 (class 2606 OID 17096)
-- Name: products_seo FK_68; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_seo
  ADD CONSTRAINT "FK_68" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3691 (class 2606 OID 17101)
-- Name: text_pages_seo FK_69; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_pages_seo
  ADD CONSTRAINT "FK_69" FOREIGN KEY (text_pages_id) REFERENCES public.text_pages(text_pages_id);


--
-- TOC entry 3686 (class 2606 OID 17106)
-- Name: products_pricing FK_7; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products_pricing
  ADD CONSTRAINT "FK_7" FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- TOC entry 3689 (class 2606 OID 17111)
-- Name: text_page_urls FK_70; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_page_urls
  ADD CONSTRAINT "FK_70" FOREIGN KEY (url_key, upper_key) REFERENCES public.url_keys(url_key, upper_key);


--
-- TOC entry 3690 (class 2606 OID 17116)
-- Name: text_page_urls FK_71; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.text_page_urls
  ADD CONSTRAINT "FK_71" FOREIGN KEY (text_pages_id) REFERENCES public.text_pages(text_pages_id);


--
-- TOC entry 3616 (class 2606 OID 17121)
-- Name: blocks FK_72; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blocks
  ADD CONSTRAINT "FK_72" FOREIGN KEY (template_key) REFERENCES public.templates(template_key);


--
-- TOC entry 3613 (class 2606 OID 17126)
-- Name: block_id FK_73; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.block_id
  ADD CONSTRAINT "FK_73" FOREIGN KEY (block_id) REFERENCES public.backend_block(block_id) NOT VALID;


--
-- TOC entry 3614 (class 2606 OID 17131)
-- Name: block_id FK_74; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.block_id
  ADD CONSTRAINT "FK_74" FOREIGN KEY (category_id) REFERENCES public.categories(category_id) NOT VALID;


--
-- TOC entry 3615 (class 2606 OID 17136)
-- Name: block_id FK_75; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.block_id
  ADD CONSTRAINT "FK_75" FOREIGN KEY (product_id) REFERENCES public.products(product_id) NOT VALID;


--
-- TOC entry 3610 (class 2606 OID 17141)
-- Name: attribute_block FK_76; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attribute_block
  ADD CONSTRAINT "FK_76" FOREIGN KEY (block_id) REFERENCES public.backend_block(block_id);


--
-- TOC entry 3611 (class 2606 OID 17146)
-- Name: attribute_block FK_77; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attribute_block
  ADD CONSTRAINT "FK_77" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id);


--
-- TOC entry 3625 (class 2606 OID 17151)
-- Name: eav_attribute_bool FK_78; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_bool
  ADD CONSTRAINT "FK_78" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3626 (class 2606 OID 17156)
-- Name: eav_attribute_float FK_79; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_float
  ADD CONSTRAINT "FK_79" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3681 (class 2606 OID 17161)
-- Name: multi_select_attributes_int FK_8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_int
  ADD CONSTRAINT "FK_8" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3627 (class 2606 OID 17166)
-- Name: eav_attribute_int FK_80; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_int
  ADD CONSTRAINT "FK_80" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3628 (class 2606 OID 17171)
-- Name: eav_attribute_money FK_81; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_money
  ADD CONSTRAINT "FK_81" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3629 (class 2606 OID 17176)
-- Name: eav_attribute_multi_select FK_82; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_multi_select
  ADD CONSTRAINT "FK_82" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3630 (class 2606 OID 17181)
-- Name: eav_attribute_string FK_83; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eav_attribute_string
  ADD CONSTRAINT "FK_83" FOREIGN KEY (attribute_id) REFERENCES public.block_attributes(attribute_id) NOT VALID;


--
-- TOC entry 3693 (class 2606 OID 17214)
-- Name: image_product FK_85; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.image_product
  ADD CONSTRAINT "FK_85" FOREIGN KEY (product_id) REFERENCES public.products(product_id) NOT VALID;


--
-- TOC entry 3694 (class 2606 OID 17219)
-- Name: image_product FK_86; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.image_product
  ADD CONSTRAINT "FK_86" FOREIGN KEY (image_url) REFERENCES public.image(image_url) NOT VALID;


--
-- TOC entry 3680 (class 2606 OID 17186)
-- Name: multi_select_attributes_float FK_9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.multi_select_attributes_float
  ADD CONSTRAINT "FK_9" FOREIGN KEY (attribute_key) REFERENCES public.custom_product_attributes(attribute_key);


--
-- TOC entry 3904 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2025-03-19 12:24:13

--
-- PostgreSQL database dump complete
--

