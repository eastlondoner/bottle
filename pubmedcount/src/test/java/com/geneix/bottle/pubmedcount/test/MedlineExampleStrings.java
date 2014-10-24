package com.geneix.bottle.pubmedcount.test;

import com.geneix.bottle.PubMedCount;

/**
 * Created by Andrew on 24/10/2014.
 */
public class MedlineExampleStrings {
    /**
     *
     * @return Array of individual entries. The first returned (index 0) just contains a new line
     */
    static final String[] getExamplesAsArray(){
        return EXAMPLE_2.split(PubMedCount.getDelimiter());
    }

    static final String EXAMPLE_TEXT =
            "\n" +
                    "PMID- 25311385\n" +
                    "OWN - NLM\n";
    static final String EXAMPLE_2 =
                            EXAMPLE_TEXT +
                                    "STAT- Publisher\n" +
                                    "DA  - 20141014\n" +
                                    "LR  - 20141015\n" +
                                    "IS  - 1473-1150 (Electronic)\n" +
                                    "IS  - 1470-269X (Linking)\n" +
                                    "DP  - 2014 Oct 14\n" +
                                    "TI  - Opioid-induced respiratory depression: ABCB1 transporter pharmacogenetics.\n" +
                                    "LID - 10.1038/tpj.2014.56 [doi]\n" +
                                    "AB  - Opioid-related respiratory depression (RD) is a serious clinical problem as it\n" +
                                    "      causes multiple deaths and anoxic brain injuries. Morphine is subject to efflux\n" +
                                    "      via P-glycoprotein transporter encoded by ABCB1, also known as MDR1. ABCB1\n" +
                                    "      polymorphisms may affect blood-brain barrier transport of morphine and therefore\n" +
                                    "      individual response to its central analgesic and adverse effects. This study\n" +
                                    "      aimed to determine specific associations between common ABCB1 genetic variants\n" +
                                    "      and clinically important outcomes including RD and RD resulting in prolonged stay\n" +
                                    "      in hospital with intravenous morphine in a homogenous pediatric surgical pain\n" +
                                    "      population of 263 children undergoing tonsillectomy. Children with GG and GA\n" +
                                    "      genotypes of ABCB1 polymorphism rs9282564 had higher risks of RD resulting in\n" +
                                    "      prolonged hospital stays; adding one copy of the minor allele (G) increased the\n" +
                                    "      odds of prolonged hospital stay due to postoperative RD by 4.7-fold (95%\n" +
                                    "      confidence interval: 2.1-10.8, P=0.0002).The Pharmacogenomics Journal advance\n" +
                                    "      online publication, 14 October 2014; doi:10.1038/tpj.2014.56.\n" +
                                    "FAU - Sadhasivam, S\n" +
                                    "AU  - Sadhasivam S\n" +
                                    "AD  - 1] Department of Anesthesia, Cincinnati Children's Hospital Medical Center,\n" +
                                    "      Cincinnati, OH, USA [2] Department of Pediatrics, Cincinnati Children's Hospital\n" +
                                    "      Medical Center, Cincinnati, OH, USA.\n" +
                                    "FAU - Chidambaran, V\n" +
                                    "AU  - Chidambaran V\n" +
                                    "AUID- ORCID: 0000000179131932\n" +
                                    "AD  - 1] Department of Anesthesia, Cincinnati Children's Hospital Medical Center,\n" +
                                    "      Cincinnati, OH, USA [2] Department of Pediatrics, Cincinnati Children's Hospital\n" +
                                    "      Medical Center, Cincinnati, OH, USA.\n" +
                                    "FAU - Zhang, X\n" +
                                    "AU  - Zhang X\n" +
                                    "AD  - 1] Department of Pediatrics, Cincinnati Children's Hospital Medical Center,\n" +
                                    "      Cincinnati, OH, USA [2] Division of Human Genetics, Cincinnati Children's\n" +
                                    "      Hospital Medical Center, Cincinnati, OH, USA.\n" +
                                    "FAU - Meller, J\n" +
                                    "AU  - Meller J\n" +
                                    "AD  - 1] Department of Pediatrics, Cincinnati Children's Hospital Medical Center,\n" +
                                    "      Cincinnati, OH, USA [2] Division of Bioinformatics, Cincinnati Children's\n" +
                                    "      Hospital Medical Center, Cincinnati, OH, USA.\n" +
                                    "FAU - Esslinger, H\n" +
                                    "AU  - Esslinger H\n" +
                                    "AD  - Department of Anesthesia, Cincinnati Children's Hospital Medical Center,\n" +
                                    "      Cincinnati, OH, USA.\n" +
                                    "FAU - Zhang, K\n" +
                                    "AU  - Zhang K\n" +
                                    "AD  - 1] Department of Pediatrics, Cincinnati Children's Hospital Medical Center,\n" +
                                    "      Cincinnati, OH, USA [2] Division of Human Genetics, Cincinnati Children's\n" +
                                    "      Hospital Medical Center, Cincinnati, OH, USA.\n" +
                                    "FAU - Martin, L J\n" +
                                    "AU  - Martin LJ\n" +
                                    "AD  - 1] Department of Pediatrics, Cincinnati Children's Hospital Medical Center,\n" +
                                    "      Cincinnati, OH, USA [2] Division of Human Genetics, Cincinnati Children's\n" +
                                    "      Hospital Medical Center, Cincinnati, OH, USA.\n" +
                                    "FAU - McAuliffe, J\n" +
                                    "AU  - McAuliffe J\n" +
                                    "AD  - 1] Department of Anesthesia, Cincinnati Children's Hospital Medical Center,\n" +
                                    "      Cincinnati, OH, USA [2] Department of Pediatrics, Cincinnati Children's Hospital\n" +
                                    "      Medical Center, Cincinnati, OH, USA.\n" +
                                    "LA  - ENG\n" +
                                    "PT  - JOURNAL ARTICLE\n" +
                                    "DEP - 20141014\n" +
                                    "TA  - Pharmacogenomics J\n" +
                                    "JT  - The pharmacogenomics journal\n" +
                                    "JID - 101083949\n" +
                                    "EDAT- 2014/10/15 06:00\n" +
                                    "MHDA- 2014/10/15 06:00\n" +
                                    "CRDT- 2014/10/15 06:00\n" +
                                    "PHST- 2014/06/16 [received]\n" +
                                    "PHST- 2014/08/18 [revised]\n" +
                                    "PHST- 2014/08/21 [accepted]\n" +
                                    "AID - tpj201456 [pii]\n" +
                                    "AID - 10.1038/tpj.2014.56 [doi]\n" +
                                    "PST - aheadofprint\n" +
                                    "SO  - Pharmacogenomics J. 2014 Oct 14. doi: 10.1038/tpj.2014.56.\n" +
                                    "\n";
    private final static String EXAMPLE_3 = EXAMPLE_2 +
            "PMID- 25303302\n" +
            "OWN - NLM\n" +
            "STAT- In-Data-Review\n" +
            "DA  - 20141011\n" +
            "IS  - 1744-8042 (Electronic)\n" +
            "IS  - 1462-2416 (Linking)\n" +
            "VI  - 15\n" +
            "IP  - 11\n" +
            "DP  - 2014 Aug\n" +
            "TI  - Pharmacogenetics of erectile dysfunction: navigating into uncharted waters.\n" +
            "PG  - 1519-38\n" +
            "LID - 10.2217/pgs.14.110 [doi]\n" +
            "AB  - Sildenafil and other PDE-5 inhibitors have revolutionized erectile dysfunction\n" +
            "      (ED) treatment. However, a significant number of patients do not respond or\n" +
            "      present adverse reactions to these drugs. While genetic polymorphisms may\n" +
            "      underlie this phenomenon, very little research has been undertaken in this\n" +
            "      research field. Most of the current knowledge is based on sildenafil, thus almost\n" +
            "      completely ignoring other important pharmacological therapies. Currently, the\n" +
            "      most promising genes with pharmacogenetic implications in ED are related to the\n" +
            "      nitric oxide and cGMP pathway, although other genes are likely to affect the\n" +
            "      responsiveness to treatment of ED. Nevertheless, the small number of studies\n" +
            "      available opens the possibility of further exploring other genes and phenotypes\n" +
            "      related to ED. This article provides a comprehensive overview of the genes being\n" +
            "      tested for their pharmacogenetic relevance in the therapy of ED.\n" +
            "FAU - Lacchini, Riccardo\n" +
            "AU  - Lacchini R\n" +
            "AD  - Department of Psychiatric Nursing & Human Sciences, Ribeirao Preto College of\n" +
            "      Nursing, University of Sao Paulo, Ribeirao Preto, Brazil.\n" +
            "FAU - Tanus-Santos, Jose E\n" +
            "AU  - Tanus-Santos JE\n" +
            "LA  - eng\n" +
            "PT  - Journal Article\n" +
            "PL  - England\n" +
            "TA  - Pharmacogenomics\n" +
            "JT  - Pharmacogenomics\n" +
            "JID - 100897350\n" +
            "SB  - IM\n" +
            "OTO - NOTNLM\n" +
            "OT  - PDE-5 inhibitors\n" +
            "OT  - erectile dysfunction\n" +
            "OT  - pharmacogenetics\n" +
            "OT  - pharmacogenomics\n" +
            "OT  - sildenafil\n" +
            "EDAT- 2014/10/11 06:00\n" +
            "MHDA- 2014/10/11 06:00\n" +
            "CRDT- 2014/10/11 06:00\n" +
            "AID - 10.2217/pgs.14.110 [doi]\n" +
            "PST - ppublish\n" +
            "SO  - Pharmacogenomics. 2014 Aug;15(11):1519-38. doi: 10.2217/pgs.14.110.\n" +
            "\n" +
            "PMID- 25303295\n" +
            "OWN - NLM\n" +
            "STAT- In-Data-Review\n" +
            "DA  - 20141011\n" +
            "IS  - 1744-8042 (Electronic)\n" +
            "IS  - 1462-2416 (Linking)\n" +
            "VI  - 15\n" +
            "IP  - 11\n" +
            "DP  - 2014 Aug\n" +
            "TI  - From pharmacogenetics to pharmacometabolomics: SAM modulates TPMT activity.\n" +
            "PG  - 1437-49\n" +
            "LID - 10.2217/pgs.14.84 [doi]\n" +
            "AB  - AIM: In the present study, the influence of SAM on TPMT activity in vivo on human\n" +
            "      subjects was investigated. SUBJECTS & METHODS: A total of 1017 donors from the\n" +
            "      Estonian Genome Center of the University of Tartu (Estonia) were genotyped for\n" +
            "      common TPMT variants, evaluated for TPMT activity, SAM levels, a set of 19\n" +
            "      biochemical and ten hematological parameters and demographic data. RESULTS: After\n" +
            "      adjustment in multiple regression models and correction for multiple testing,\n" +
            "      from the 43 factors that were tested, only TPMT genotype (p = 1 x 10(-13)) and\n" +
            "      SAM levels (p = 1 x 10(-13)) were found to significantly influence TPMT activity.\n" +
            "      The influence of SAM on TPMT activity was more pronounced in TPMT-heterozygous\n" +
            "      than wild-type individuals. CONCLUSION: SAM represents a potential\n" +
            "      pharmacometabolomic marker and therapeutic agent in TPMT-heterozygous subjects.\n" +
            "      Original submitted 17 February 2014; Revision submitted 16 May 2014.\n" +
            "FAU - Karas-Kuzelicki, Natasa\n" +
            "AU  - Karas-Kuzelicki N\n" +
            "AD  - Faculty of Pharmacy, University of Ljubljana, Askerceva 7, 1000 Ljubljana,\n" +
            "      Slovenia.\n" +
            "FAU - Smid, Alenka\n" +
            "AU  - Smid A\n" +
            "FAU - Tamm, Riin\n" +
            "AU  - Tamm R\n" +
            "FAU - Metspalu, Andres\n" +
            "AU  - Metspalu A\n" +
            "FAU - Mlinaric-Rascan, Irena\n" +
            "AU  - Mlinaric-Rascan I\n" +
            "LA  - eng\n" +
            "PT  - Journal Article\n" +
            "PL  - England\n" +
            "TA  - Pharmacogenomics\n" +
            "JT  - Pharmacogenomics\n" +
            "JID - 100897350\n" +
            "SB  - IM\n" +
            "OTO - NOTNLM\n" +
            "OT  - 6-mercaptopurine\n" +
            "OT  - ALL\n" +
            "OT  - S-adenosyl methionine\n" +
            "OT  - pharmacogenetics\n" +
            "OT  - thiopurine-S-methyltransferase\n" +
            "EDAT- 2014/10/11 06:00\n" +
            "MHDA- 2014/10/11 06:00\n" +
            "CRDT- 2014/10/11 06:00\n" +
            "AID - 10.2217/pgs.14.84 [doi]\n" +
            "PST - ppublish\n" +
            "SO  - Pharmacogenomics. 2014 Aug;15(11):1437-49. doi: 10.2217/pgs.14.84.\n" +
            "\n";
}
