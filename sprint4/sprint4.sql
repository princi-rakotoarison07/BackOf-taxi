--
-- PostgreSQL database dump
--

\restrict Mg4B6NfVZD42mO722NSOOaaKH8cTQbXQEsLYhDLy0VEgRxNXI4K8g2X5N8SauYn

-- Dumped from database version 15.14
-- Dumped by pg_dump version 15.14

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: distance; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.distance (
    id integer NOT NULL,
    idhotelfrom integer NOT NULL,
    idhotelto integer NOT NULL,
    valeur numeric(10,2) DEFAULT 0 NOT NULL
);


ALTER TABLE public.distance OWNER TO postgres;

--
-- Name: distance_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.distance_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.distance_id_seq OWNER TO postgres;

--
-- Name: distance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.distance_id_seq OWNED BY public.distance.id;


--
-- Name: hotel; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hotel (
    id integer NOT NULL,
    nom character varying(255) NOT NULL,
    distanceaeroport numeric(10,2) DEFAULT 10 NOT NULL
);


ALTER TABLE public.hotel OWNER TO postgres;

--
-- Name: hotel_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.hotel_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hotel_id_seq OWNER TO postgres;

--
-- Name: hotel_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.hotel_id_seq OWNED BY public.hotel.id;


--
-- Name: parametre; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parametre (
    id integer NOT NULL,
    libelle character varying(255) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.parametre OWNER TO postgres;

--
-- Name: parametre_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.parametre_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parametre_id_seq OWNER TO postgres;

--
-- Name: parametre_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.parametre_id_seq OWNED BY public.parametre.id;


--
-- Name: reservation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reservation (
    id integer NOT NULL,
    idclient character varying(4) NOT NULL,
    nombrepassagers integer NOT NULL,
    dateheurearrivee timestamp without time zone NOT NULL,
    idhotel integer NOT NULL
);


ALTER TABLE public.reservation OWNER TO postgres;

--
-- Name: reservation_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.reservation_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.reservation_id_seq OWNER TO postgres;

--
-- Name: reservation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.reservation_id_seq OWNED BY public.reservation.id;


--
-- Name: trajet; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.trajet (
    id integer NOT NULL,
    idvoiture integer NOT NULL,
    idreservation integer NOT NULL,
    dateheuredepart timestamp without time zone NOT NULL,
    dateheurearrivee timestamp without time zone NOT NULL,
    idlieuarrivee integer,
    idlieudepart integer
);


ALTER TABLE public.trajet OWNER TO postgres;

--
-- Name: trajet_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.trajet_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.trajet_id_seq OWNER TO postgres;

--
-- Name: trajet_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.trajet_id_seq OWNED BY public.trajet.id;


--
-- Name: typeenergie; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.typeenergie (
    id integer NOT NULL,
    code character varying(10) NOT NULL,
    libelle character varying(20) NOT NULL
);


ALTER TABLE public.typeenergie OWNER TO postgres;

--
-- Name: typeenergie_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.typeenergie_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.typeenergie_id_seq OWNER TO postgres;

--
-- Name: typeenergie_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.typeenergie_id_seq OWNED BY public.typeenergie.id;


--
-- Name: voiture; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.voiture (
    id integer NOT NULL,
    numero character varying(20) NOT NULL,
    nbplace integer NOT NULL,
    idtypeenergie integer NOT NULL,
    CONSTRAINT voiture_nbplace_check CHECK ((nbplace > 0))
);


ALTER TABLE public.voiture OWNER TO postgres;

--
-- Name: voiture_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.voiture_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.voiture_id_seq OWNER TO postgres;

--
-- Name: voiture_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.voiture_id_seq OWNED BY public.voiture.id;


--
-- Name: distance id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distance ALTER COLUMN id SET DEFAULT nextval('public.distance_id_seq'::regclass);


--
-- Name: hotel id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hotel ALTER COLUMN id SET DEFAULT nextval('public.hotel_id_seq'::regclass);


--
-- Name: parametre id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parametre ALTER COLUMN id SET DEFAULT nextval('public.parametre_id_seq'::regclass);


--
-- Name: reservation id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservation ALTER COLUMN id SET DEFAULT nextval('public.reservation_id_seq'::regclass);


--
-- Name: trajet id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trajet ALTER COLUMN id SET DEFAULT nextval('public.trajet_id_seq'::regclass);


--
-- Name: typeenergie id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.typeenergie ALTER COLUMN id SET DEFAULT nextval('public.typeenergie_id_seq'::regclass);


--
-- Name: voiture id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.voiture ALTER COLUMN id SET DEFAULT nextval('public.voiture_id_seq'::regclass);


--
-- Data for Name: distance; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.distance VALUES (1, 1, 2, 3.00);
INSERT INTO public.distance VALUES (2, 1, 3, 6.00);
INSERT INTO public.distance VALUES (3, 2, 3, 3.00);


--
-- Data for Name: hotel; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.hotel VALUES (2, 'Hotel B', 5.00);
INSERT INTO public.hotel VALUES (3, 'Hotel C', 8.00);
INSERT INTO public.hotel VALUES (1, 'Hotel A', 6.00);


--
-- Data for Name: parametre; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.parametre VALUES (2, 'VitesseKmh', '50');
INSERT INTO public.parametre VALUES (1, 'AttenteMinute', '30');


--
-- Data for Name: reservation; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.reservation VALUES (2, 'C002', 10, '2026-03-04 08:00:00', 1);
INSERT INTO public.reservation VALUES (3, 'C005', 2, '2026-03-04 08:00:00', 2);
INSERT INTO public.reservation VALUES (4, 'C007', 4, '2026-03-04 08:00:00', 3);
INSERT INTO public.reservation VALUES (1, 'C001', 5, '2026-03-04 08:00:00', 3);


--
-- Data for Name: trajet; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: typeenergie; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.typeenergie VALUES (1, 'SP', 'Essence');
INSERT INTO public.typeenergie VALUES (2, 'GS', 'Diesel');
INSERT INTO public.typeenergie VALUES (3, 'EL', 'Electrique');


--
-- Data for Name: voiture; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.voiture VALUES (4, 'V003', 15, 1);
INSERT INTO public.voiture VALUES (2, 'V001', 7, 2);
INSERT INTO public.voiture VALUES (3, 'V002', 8, 2);


--
-- Name: distance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.distance_id_seq', 3, true);


--
-- Name: hotel_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.hotel_id_seq', 3, true);


--
-- Name: parametre_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.parametre_id_seq', 1, false);


--
-- Name: reservation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.reservation_id_seq', 1, false);


--
-- Name: trajet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.trajet_id_seq', 4, true);


--
-- Name: typeenergie_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.typeenergie_id_seq', 1, false);


--
-- Name: voiture_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.voiture_id_seq', 4, true);


--
-- Name: distance distance_idhotelfrom_idhotelto_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distance
    ADD CONSTRAINT distance_idhotelfrom_idhotelto_key UNIQUE (idhotelfrom, idhotelto);


--
-- Name: distance distance_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distance
    ADD CONSTRAINT distance_pkey PRIMARY KEY (id);


--
-- Name: hotel hotel_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hotel
    ADD CONSTRAINT hotel_pkey PRIMARY KEY (id);


--
-- Name: parametre parametre_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parametre
    ADD CONSTRAINT parametre_pkey PRIMARY KEY (id);


--
-- Name: reservation reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_pkey PRIMARY KEY (id);


--
-- Name: trajet trajet_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trajet
    ADD CONSTRAINT trajet_pkey PRIMARY KEY (id);


--
-- Name: typeenergie typeenergie_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.typeenergie
    ADD CONSTRAINT typeenergie_pkey PRIMARY KEY (id);


--
-- Name: voiture voiture_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.voiture
    ADD CONSTRAINT voiture_pkey PRIMARY KEY (id);


--
-- Name: distance distance_idhotelfrom_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distance
    ADD CONSTRAINT distance_idhotelfrom_fkey FOREIGN KEY (idhotelfrom) REFERENCES public.hotel(id);


--
-- Name: distance distance_idhotelto_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distance
    ADD CONSTRAINT distance_idhotelto_fkey FOREIGN KEY (idhotelto) REFERENCES public.hotel(id);


--
-- Name: reservation reservation_idhotel_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_idhotel_fkey FOREIGN KEY (idhotel) REFERENCES public.hotel(id);


--
-- Name: trajet trajet_idlieuarrivee_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trajet
    ADD CONSTRAINT trajet_idlieuarrivee_fkey FOREIGN KEY (idlieuarrivee) REFERENCES public.hotel(id);


--
-- Name: trajet trajet_idlieudepart_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trajet
    ADD CONSTRAINT trajet_idlieudepart_fkey FOREIGN KEY (idlieudepart) REFERENCES public.hotel(id);


--
-- Name: trajet trajet_idreservation_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trajet
    ADD CONSTRAINT trajet_idreservation_fkey FOREIGN KEY (idreservation) REFERENCES public.reservation(id);


--
-- Name: trajet trajet_idvoiture_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trajet
    ADD CONSTRAINT trajet_idvoiture_fkey FOREIGN KEY (idvoiture) REFERENCES public.voiture(id);


--
-- Name: voiture voiture_idtypeenergie_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.voiture
    ADD CONSTRAINT voiture_idtypeenergie_fkey FOREIGN KEY (idtypeenergie) REFERENCES public.typeenergie(id);


--
-- PostgreSQL database dump complete
--

\unrestrict Mg4B6NfVZD42mO722NSOOaaKH8cTQbXQEsLYhDLy0VEgRxNXI4K8g2X5N8SauYn

