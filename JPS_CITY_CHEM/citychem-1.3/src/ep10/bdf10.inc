! Generated by GenChem.py - DO NOT EDIT

!-> OP

  P = RC(2) * X(OD,2)  &
    + DJ(2) * X(O3,2)  &
    + DJ(3) * X(NO2,2)
  
  L = RC(1)  &
    + RC(3) * X(NO,2)
  
  X(OP,2) = P/L


!-> OD

  P = DJ(1) * X(O3,2)
  
  L = RC(2)  &
    + RC(4)
  
  X(OD,2) = P/L


!-> OH

  P = 2.* RC(4) * X(OD,2)  &
    + RC(7) * X(O3,2) * X(HO2,2)  &
    + RC(8) * X(NO,2) * X(HO2,2)  &
    + DJ(5) * X(HNO3,2)
  
  L = RC(6) * X(O3,2)  &
    + RC(9) * X(NO2,2)  &
    + RC(10) * X(HCHO,2)  &
    + RC(11) * X(CO,2)
  
  X(OH,2) = P/L


!-> HO2

  P = RC(6) * X(O3,2) * X(OH,2)  &
    + RC(10) * X(OH,2) * X(HCHO,2)  &
    + RC(11) * X(OH,2) * X(CO,2)  &
    + 2* DJ(6) * X(HCHO,2)
  
  L = RC(7) * X(O3,2)  &
    + RC(8) * X(NO,2)
  
  X(HO2,2) = (A1*X(HO2,1)+A2*X(HO2,0)+A3*Ldt*P)/(A4+A3*Ldt*L)


!-> O3

  P = RC(1) * X(OP,2)
  
  L = RC(5) * X(NO,2)  &
    + RC(6) * X(OH,2)  &
    + RC(7) * X(HO2,2)  &
    + DJ(1)  &
    + DJ(2)  &
    + RC(12)
  
  X(O3,2) = (A1*X(O3,1)+A2*X(O3,0)+A3*Ldt*P)/(A4+A3*Ldt*L)


!-> NO

  P = DJ(3) * X(NO2,2)
  
  L = RC(3) * X(OP,2)  &
    + RC(5) * X(O3,2)  &
    + RC(8) * X(HO2,2)
  
  X(NO,2) = (A1*X(NO,1)+A2*X(NO,0)+A3*Ldt*P)/(A4+A3*Ldt*L)


!-> NO2

  P = RC(3) * X(OP,2) * X(NO,2)  &
    + RC(5) * X(O3,2) * X(NO,2)  &
    + RC(8) * X(NO,2) * X(HO2,2)  &
    + DJ(5) * X(HNO3,2)
  
  L = RC(9) * X(OH,2)  &
    + DJ(3)  &
    + RC(13)
  
  X(NO2,2) = (A1*X(NO2,1)+A2*X(NO2,0)+A3*Ldt*P)/(A4+A3*Ldt*L)


!-> HNO3

  P = RC(9) * X(NO2,2) * X(OH,2)
  
  L = DJ(5)  &
    + RC(14)  &
    + RC(16)
  
  X(HNO3,2) = (A1*X(HNO3,1)+A2*X(HNO3,0)+A3*Ldt*P)/(A4+A3*Ldt*L)


!-> HCHO

  ! P = 0.0
  
  L = RC(10) * X(OH,2)  &
    + DJ(6)  &
    + DJ(7)  &
    + RC(17)
  
  X(HCHO,2) = (A1*X(HCHO,1)+A2*X(HCHO,0))/(A4+A3*Ldt*L)


!-> CO

  P = RC(10) * X(OH,2) * X(HCHO,2)  &
    + DJ(6) * X(HCHO,2)  &
    + DJ(7) * X(HCHO,2)
  
  L = RC(11) * X(OH,2)  &
    + RC(15)
  
  X(CO,2) = (A1*X(CO,1)+A2*X(CO,0)+A3*Ldt*P)/(A4+A3*Ldt*L)


