/*!
    * Start Bootstrap - Freelancer v6.0.4 (https://startbootstrap.com/themes/freelancer)
    * Copyright 2013-2020 Start Bootstrap
    * Licensed under MIT (https://github.com/StartBootstrap/startbootstrap-freelancer/blob/master/LICENSE)
    */

// TODO: separate the visualization for JPS/ wolfram + google ...

vibration_frequency = [
    "H 2 ",
    "H 2 O 2 ",
    "H 2 O 1 ",
    "H 1 O 2 ",
    "N 2 ",
    "O 2 ",
    "H 1 O 1 ",
    "Cl 2 ",
    "Cl 4 Ti 1 ",
    "Cl 2 O 2 Ti 1 ",
    "Cl 2 O 6 ",
    "Cl 2 O 10 Ti 3 ",
    "Cl 1 O 1 Ti 1 ",
    "C 6 H 13 O 4 Ti 1 ",
    "C 3 H 10 O 4 Ti 1 ",
    "C 3 H 8 ",
    "C 2 H 4 ",
    "C 2 H 4 O 1 ",
    "C 2 H 6 ",
    "C 2 H 6 O 1 ",
    "C 3 H 6 ",
    "C 1 H 4 ",
    "C 1 H 4 O 1 ",
    "Cl 2 O 1 ",
    "Cl 1 O 2 ",
    "Cl 1 O 4 ",
    "Cl 1 Ti 1 ",
    "Cl 3 Ti 1 ",
    "Cl 2 O 7 ",
    "C 1 H 1 ",
    "Cl 2 O 4 ",
    "C 4 H 6 O 1 ",
    "C 6 H 12 ",
    "C 4 H 10 ",
    "Cl 2 Ti 1 ",
    "C 3 H 4 O 1 S 2 ",
    "C 1 H 2 O 1 ",
    "C 22 H 14 ",
    "C 12 H 8 ",
    "C 12 H 7 ",
    "C 12 H 9 ",
    "C 12 H 10 ",
    "C 6 H 10 ",
    "C 5 H 12 O 1 ",
    "C 8 H 10 O 1 ",
    "C 8 H 18 ",
    "C 5 H 6 ",
    "C 7 H 10 O 1 ",
    "C 8 H 12 ",
    "C 10 H 20 ",
    "C 3 H 4 O 2 ",
    "C 9 H 20 ",
    "C 10 H 16 ",
    "C 14 H 12 ",
    "C 11 H 10 ",
    "C 11 H 18 ",
    "C 13 H 20 O 1 ",
    "C 30 H 46 ",
    "C 3 H 7 ",
    "C 5 H 10 O 2 ",
    "C 9 H 18 O 2 ",
    "C 7 H 14 ",
    "C 7 H 16 O 1 ",
    "C 22 H 26 ",
    "C 12 H 16 ",
    "C 5 H 8 O 1 ",
    "C 10 H 18 ",
    "C 26 H 54 ",
    "C 4 H 4 O 2 ",
    "C 7 H 12 O 1 ",
    "C 6 H 12 O 1 ",
    "C 11 H 22 O 2 ",
    "C 9 H 20 O 1 ",
    "C 2 H 4 O 2 ",
    "C 6 H 6 O 1 ",
    "C 4 H 4 ",
    "C 5 H 12 ",
    "C 10 H 16 O 1 ",
    "C 9 H 16 O 2 ",
    "C 18 H 12 ",
    "C 14 H 30 ",
    "C 5 H 10 O 1 ",
    "C 8 H 14 ",
    "C 12 H 22 ",
    "C 7 H 8 ",
    "C 18 H 20 ",
    "C 4 H 10 O 2 ",
    "C 4 H 10 O 1 ",
    "C 3 H 8 O 3 ",
    "C 8 H 16 O 2 ",
    "C 8 H 14 O 2 ",
    "C 7 H 12 ",
    "C 10 H 12 ",
    "C 2 H 2 O 1 ",
    "C 13 H 12 ",
    "C 3 H 8 O 2 ",
    "C 6 H 14 ",
    "C 9 H 12 ",
    "C 8 H 10 ",
    "C 10 H 8 ",
    "C 5 H 12 O 4 ",
    "C 10 H 22 ",
    "C 11 H 16 ",
    "C 3 H 6 O 1 ",
    "C 9 H 16 ",
    "C 15 H 22 O 2 ",
    "C 10 H 14 ",
    "C 6 H 14 O 1 ",
    "C 10 H 8 O 2 ",
    "C 34 H 54 ",
    "C 10 H 12 O 2 ",
    "C 4 H 9 ",
    "C 12 H 20 ",
    "C 5 H 10 O 3 ",
    "C 7 H 12 O 2 ",
    "C 1 H 3 O 1 ",
    "C 7 H 10 ",
    "C 7 H 14 O 2 ",
    "C 6 H 10 O 2 ",
    "C 14 H 16 ",
    "C 9 H 18 O 3 ",
    "C 6 H 6 ",
    "C 8 H 8 O 2 ",
    "C 12 H 18 O 1 ",
    "C 6 H 14 O 2 ",
    "C 8 H 8 O 3 ",
    "C 20 H 12 ",
    "C 16 H 16 ",
    "C 10 H 10 ",
    "C 5 H 10 ",
    "C 6 H 12 O 2 ",
    "C 8 H 16 ",
    "C 7 H 12 O 4 ",
    "C 3 H 6 O 2 ",
    "C 16 H 14 ",
    "C 4 H 4 O 4 ",
    "C 9 H 14 ",
    "C 6 H 8 ",
    "C 14 H 14 ",
    "C 7 H 16 ",
    "C 20 H 16 ",
    "C 4 H 6 O 3 ",
    "C 5 H 8 ",
    "C 16 H 10 ",
    "C 10 H 18 O 1 ",
    "C 9 H 10 O 2 ",
    "C 16 H 12 ",
    "C 3 H 8 O 1 ",
    "C 24 H 32 ",
    "C 13 H 14 ",
    "C 10 H 18 O 2 ",
    "C 4 H 6 ",
    "C 2 H 3 ",
    "C 5 H 8 O 2 ",
    "C 5 H 12 O 2 ",
    "C 4 H 8 ",
    "C 12 H 24 ",
    "C 8 H 14 O 1 ",
    "C 28 H 18 ",
    "C 8 H 8 ",
    "C 1 H 2 ",
    "C 3 H 4 ",
    "C 24 H 18 ",
    "C 12 H 8 O 1 ",
    "C 7 H 16 O 3 ",
    "C 12 H 26 O 1 ",
    "C 19 H 16 ",
    "C 16 H 32 ",
    "C 12 H 18 ",
    "C 10 H 14 O 1 ",
    "C 18 H 36 ",
    "C 7 H 8 O 1 ",
    "C 8 H 16 O 4 ",
    "C 8 H 16 O 1 ",
    "C 7 H 10 O 2 ",
    "C 4 H 8 O 2 ",
    "C 12 H 12 ",
    "C 18 H 38 ",
    "C 26 H 26 ",
    "C 20 H 30 ",
    "C 12 H 8 O 2 ",
    "C 8 H 12 O 1 ",
    "C 10 H 8 O 1 ",
    "C 12 H 26 ",
    "C 1 O 1 ",
    "C 9 H 16 O 1 ",
    "C 13 H 18 ",
    "C 21 H 42 ",
    "C 9 H 18 ",
    "C 13 H 28 ",
    "C 4 H 6 O 2 ",
    "O 1 Ti 1 ",
    "C 4 H 8 O 1 ",
    "C 8 H 18 O 1 ",
    "C 13 H 26 ",
    "C 6 H 10 O 3 ",
    "C 12 H 14 ",
    "C 6 H 10 O 1 ",
    "C 9 H 10 ",
    "C 9 H 6 O 2 ",
    "C 18 H 18 ",
    "C 11 H 18 O 2 ",
    "C 3 H 3 ",
    "C 11 H 20 O 2 ",
    "C 10 H 10 O 4 ",
    "C 4 H 8 O 3 ",
    "C 7 H 6 O 1 ",
    "C 8 H 6 O 1 ",
    "C 8 H 18 O 2 ",
    "C 11 H 14 O 1 ",
    "C 4 H 2 ",
    "C 12 H 24 O 2 ",
    "C 15 H 22 ",
    "C 13 H 10 O 1 ",
    "C 17 H 34 ",
    "C 19 H 40 ",
    "C 3 H 5 ",
    "C 6 H 10 O 4 ",
    "C 11 H 18 O 1 ",
    "C 10 H 10 O 2 ",
    "C 10 H 10 O 1 ",
    "C 11 H 14 ",
    "C 9 H 8 ",
    "C 18 H 14 ",
    "C 12 H 16 O 2 ",
    "C 15 H 30 ",
    "C 20 H 14 O 1 ",
    "C 8 H 12 O 2 ",
    "C 21 H 26 ",
    "C 13 H 12 O 1 ",
    "C 6 H 12 O 3 ",
    "C 9 H 10 O 1 ",
    "C 2 H 6 O 2 ",
    "C 11 H 22 ",
    "C 13 H 22 ",
    "C 20 H 40 ",
    "C 6 H 8 O 2 ",
    "C 26 H 16 ",
    "C 7 H 6 O 3 ",
    "C 14 H 18 ",
    "C 4 H 2 O 4 ",
    "C 9 H 18 O 1 ",
    "C 6 H 6 O 3 ",
    "C 7 H 6 O 2 ",
    "C 31 H 64 ",
    "C 16 H 10 O 1 ",
    "C 8 H 6 O 3 ",
    "C 18 H 22 ",
    "C 14 H 10 ",
    "C 14 H 28 ",
    "C 19 H 20 ",
    "C 6 H 6 O 2 ",
    "C 3 ",
    "C 7 H 14 O 1 ",
    "C 5 H 4 O 2 ",
    "C 11 H 20 ",
    "C 22 H 38 ",
    "C 10 H 22 O 2 ",
    "C 32 H 50 ",
    "C 26 H 38 ",
    "C 1 H 2 O 2 ",
    "C 26 H 22 ",
    "C 7 H 8 O 2 ",
    "C 10 H 20 O 2 ",
    "C 11 H 24 ",
    "C 14 H 16 O 5 ",
    "C 2 H 5 ",
    "C 11 H 12 ",
    "C 20 H 36 ",
    "C 3 H 6 O 3 ",
    "C 5 H 6 O 3 ",
    "C 32 H 66 ",
    "C 3 O 2 ",
    "C 7 H 7 ",
    "C 7 H 6 ",
    "O 2 Ti 1 ",
    "C 14 H 20 ",
    "C 17 H 36 ",
    "C 3 H 2 O 3 ",
    "C 11 H 14 O 2 ",
    "C 10 H 22 O 1 ",
    "C 4 ",
    "C 10 H 6 O 4 ",
    "C 7 H 14 O 3 ",
    "C 19 H 38 ",
    "C 7 H 12 O 3 ",
    "C 16 H 34 ",
    "C 5 H 8 O 4 ",
    "C 9 H 10 O 4 ",
    "C 9 H 14 O 1 ",
    "C 14 H 8 ",
    "C 4 H 8 O 4 ",
    "C 16 H 20 ",
    "C 9 H 20 O 2 ",
    "C 22 H 44 ",
    "C 6 H 4 ",
    "C 15 H 32 ",
    "C 20 H 14 ",
    "C 10 H 6 O 2 ",
    "C 13 H 14 O 1 ",
    "C 8 H 6 ",
    "C 20 H 42 ",
    "C 5 H 12 O 3 ",
    "C 4 H 10 O 3 ",
    "C 26 H 18 ",
    "C 7 H 16 O 2 ",
    "C 12 H 26 O 2 ",
    "C 11 H 12 O 1 ",
    "C 11 H 16 O 1 ",
    "C 2 H 2 O 2 ",
    "C 7 H 10 O 3 ",
    "C 2 H 1 ",
    "C 8 H 8 O 1 ",
    "C 13 H 8 O 1 ",
    "C 1 H 3 ",
    "C 15 H 12 ",
    "C 28 H 38 ",
    "C 7 H 8 O 3 ",
    "C 1 H 1 O 1 ",
    "C 4 H 4 O 3 ",
    "C 6 H 5 ",
    "C 17 H 30 O 1 ",
    "C 6 H 14 O 3 ",
    "C 9 H 10 O 3 ",
    "C 20 H 38 ",
    "C 24 H 30 ",
    "C 13 H 10 ",
    "C 4 H 4 O 1 ",
    "C 14 H 14 O 8 ",
    "C 1 O 2 ",
    "C1O2",
    "Cl4Ti1",
    "C80H50",
    "C50H26",
    "C58H28",
    "C66H30",
    "C72H32",
    "C6H5",
    "C10H7",
    "C16H9",
    "C20H11",
    "C32H13",
    "C42H15",
    "C66H19",
    "C24H11",
    "C42H17",
    "C48H23",
    "C54H17",
    "C14H9",
    "C18H11",
    "C22H13",
    "C26H15",
    "H2",
    "C12H8",
    "C12H7",
    "C12H9",
    "C12H10",
    "C12H11",
    "C20H10",
    "C44H16",
    "C1H2O1",
    "C22H14",
    "C18H12",
    "C3H8O3",
    "C3H8O2",
    "C3H4O2",
    "C4H10O1",
    "C2H6O1",
    "C1H2O2",
    "C2H4O2",
    "C1H4O1",
    "C3H8O1",
    "C3H6O1",
    "C5H12O1",
    "C6H6",
    "C1H4",
    "C2H6",
    "C2H4",
    "C3H8",
    "C3H4",
    "C2H4O1",
    "C3H6",
    "C4H10",
    "C6H14",
    "C5H12",
    "C5H8",
    "C5H6",
    "C4H8O1",
    "C3H6O2",
    "C10H16",
    "C18H14",
    "C14H10",
    "C13H10",
    "C6H6O3",
    "C12H18",
    "C11H10",
    "C10H8",
    "C9H6O2",
    "C12H22",
    "C9H8",
    "C8H10",
    "C9H12",
    "C10H14",
    "C5H10O1",
    "C4H6O2",
    "C6H12",
    "C5H10O2",
    "C5H4O2",
    "C11H16",
    "C9H10",
    "C7H6O3",
    "C8H8O3",
    "C8H12",
    "C8H8",
    "C7H8O1",
    "C7H6O1",
    "C13H12O1",
    "C13H12",
    "C14H14",
    "C14H12",
    "C8H10O1",
    "C9H10O1",
    "C8H18O1",
    "C6H12O2",
    "C6H14O2",
    "C5H10O3",
    "C9H18O2",
    "C10H20O2",
    "C8H16O1",
    "C7H14O2",
    "C8H16O2",
    "C7H10O3",
    "C4H8",
    "C4H6",
    "C2H6O2",
    "C2H2O2",
    "C4H10O2",
    "C4H8O2",
    "C7H16",
    "C6H12O1",
    "C6H14O1",
    "C5H8O2",
    "C4H4O3",
    "C4H6O3",
    "C6H6O2",
    "C5H6O3",
    "C5H8O4",
    "C9H18O1",
    "C7H14",
    "C7H8",
    "C6H10O1",
    "C6H6O1",
    "C5H10",
    "C4H6O1",
    "C4H4O1",
    "C8H18O2",
    "C12H24O2",
    "C6H10",
    "C5H8O1",
    "C3H6O3",
    "C8H14O1",
    "C5H12O2",
    "C8H18",
    "C8H16",
    "C7H16O1",
    "C7H14O1",
    "C9H20",
    "C6H14O3",
    "C10H22O1",
    "C12H26",
    "C12H24",
    "C10H22O2",
    "C12H26O1",
    "C20H42",
    "C8H8O2",
    "C13H10O1",
    "C10H12",
    "C9H10O3",
    "C7H16O3",
    "C10H10O1",
    "C9H10O2",
    "C11H22O2",
    "C6H12O3",
    "C10H22",
    "C8H12O2",
    "C7H16O2",
    "C16H10",
    "C10H6O2",
    "C10H10O4",
    "C12H8O1",
    "C10H8O2",
    "C10H8O1",
    "C7H12O2",
    "C6H10O3",
    "C9H20O1",
    "C4H10O3",
    "C10H18",
    "C11H20",
    "C14H8",
    "C26H16",
    "C20H12",
    "C16H10O1",
    "C12H8O2",
    "C8H6O1",
    "C7H10O1",
    "C7H10",
    "C7H12",
    "C8H14",
    "C9H16",
    "C7H12O1",
    "C4H8O4",
    "C8H16O4",
    "C14H28",
    "C20H16",
    "C7H8O2",
    "C4H2",
    "C6H4",
    "C2H2O1",
    "C10H6O4",
    "C20H14",
    "C13H8O1",
    "C8H8O1",
    "C10H14O1",
    "C6H10O2",
    "C4H4O4",
    "C6H8O2",
    "C3O2",
    "C19H16",
    "C10H12O2",
    "C8H6",
    "C6H10O4",
    "C17H30O1",
    "C16H34",
    "C32H66",
    "C12H12",
    "C6H8",
    "C18H38",
    "C16H14",
    "C8H6O3",
    "C24H18",
    "C10H10",
    "C7H6O2",
    "C13H28",
    "C14H30",
    "C15H32",
    "C16H32",
    "C17H36",
    "C19H40",
    "C1O1",
    "C25H20",
    "C26H22",
    "C14H14O8",
    "C4H4O2",
    "C4H4",
    "C10H20",
    "C11H18",
    "C12H20",
    "C10H18O2",
    "C13H22",
    "C11H18O1",
    "C12H16",
    "C15H12",
    "C3H2O3",
    "C7H8O3",
    "C13H18",
    "C12H18O1",
    "C28H18",
    "C14H18",
    "C11H20O2",
    "C11H24",
    "C9H12O2",
    "C12H14",
    "C5H12O3",
    "C26H18",
    "C10H18O1",
    "C8H14O2",
    "C9H16O2",
    "C14H20",
    "C16H16",
    "C11H14O1",
    "C9H18",
    "C11H14",
    "C14H24",
    "C18H36",
    "C20H40",
    "C13H26",
    "C19H38",
    "C5H12O4",
    "C18H22",
    "C3H5",
    "C3H7",
    "C2H5",
    "C18H18",
    "C2H1",
    "C1H3O1",
    "C7H7",
    "C9H18O3",
    "C1H3",
    "C12H16O2",
    "C4H9",
    "C11H16O1",
    "C7H12O3",
    "C1H2",
    "C11H14O2",
    "C9H20O2",
    "C1H1O1",
    "C2H3",
    "C8H12O1",
    "C14H16",
    "C9H10O4",
    "C15H30",
    "C4H2O4",
    "C18H20",
    "C3H3",
    "H1O2",
    "C1H1",
    "C9H16O1",
    "H1O1",
    "C15H22",
    "C11H22",
    "C7H10O2",
    "C24H32",
    "C26H38",
    "C20H14O1",
    "C21H42",
    "C7H12O4",
    "C11H12",
    "C21H26",
    "C9H14",
    "C22H44",
    "C9H14O1",
    "O2",
    "C3",
    "O1Ti1",
    "C4",
    "C10H16O1",
    "O2Ti1",
    "C26H54",
    "C11H18O2",
    "C13H20O1",
    "C10H10O2",
    "C15H22O2",
    "C11H12O1",
    "C4H8O3",
    "C14H16O5",
    "C20H30",
    "C19H20",
    "C13H14O1",
    "C7H14O3",
    "C26H26",
    "C16H12",
    "C17H34",
    "C31H64",
    "C22H26",
    "C24H30",
    "C28H38",
    "C13H14",
    "C20H38",
    "C22H38",
    "C20H36",
    "C7H6",
    "C34H54",
    "C32H50",
    "C30H46",
    "C16H20",
    "C12H26O2",
    "Cl2",
    "Cl2O3",
    "Cl2O4",
    "Cl2O5",
    "Cl2O6",
    "Cl2O7",
    "Cl2O1",
    "Cl2O2",
    "Cl1O1",
    "Cl1O3",
    "Cl1O4",
    "Cl1O2",
    "Cl1O1Ti1",
    "Cl2O1Ti1",
    "Cl1Ti1",
    "Cl2Ti1",
    "Cl3Ti1",
    "Cl2O2Ti1",
    "Cl2O2Ti2",
    "Cl2O3Ti1",
    "Cl2O3Ti2",
    "Cl2O4Ti1",
    "Cl2O4Ti2",
    "Cl2O4Ti3",
    "Cl2O5Ti1",
    "Cl2O5Ti2",
    "Cl2O5Ti3",
    "Cl2O6Ti1",
    "Cl2O6Ti2",
    "Cl2O6Ti3",
    "Cl2O7Ti2",
    "Cl2O7Ti3",
    "Cl2O8Ti3",
    "Cl2O9Ti3",
    "Cl2O10Ti3",
    "Cl2O1Ti2",
    "Cl3O2Ti1",
    "Cl3O2Ti2",
    "Cl3O3Ti1",
    "Cl3O3Ti2",
    "Cl3O4Ti1",
    "Cl3O4Ti2",
    "Cl3O4Ti3",
    "Cl3O5Ti1",
    "Cl3O5Ti2",
    "Cl3O5Ti3",
    "Cl3O6Ti1",
    "Cl3O6Ti2",
    "Cl3O6Ti3",
    "Cl3O7Ti2",
    "Cl3O7Ti3",
    "Cl3O8Ti3",
    "Cl3O9Ti3",
    "Cl3O10Ti3",
    "Cl3O1Ti1",
    "Cl3O1Ti2",
    "Cl4O2Ti1",
    "Cl4O2Ti2",
    "Cl4O3Ti1",
    "Cl4O3Ti2",
    "Cl4O4Ti1",
    "Cl4O4Ti2",
    "Cl4O4Ti3",
    "Cl4O5Ti1",
    "Cl4O5Ti2",
    "Cl4O5Ti3",
    "Cl4O6Ti1",
    "Cl4O6Ti2",
    "Cl4O6Ti3",
    "Cl4O7Ti2",
    "Cl4O7Ti3",
    "Cl4O8Ti3",
    "Cl4O9Ti3",
    "Cl4O10Ti3",
    "Cl4O1Ti1",
    "Cl4O1Ti2",
    "Cl5O2Ti2",
    "Cl5O2Ti1",
    "Cl5O3Ti1",
    "Cl5O3Ti2",
    "Cl5O3Ti3",
    "Cl5O4Ti1",
    "Cl5O4Ti2",
    "Cl5O4Ti3",
    "Cl5O5Ti1",
    "Cl5O5Ti2",
    "Cl5O5Ti3",
    "Cl5O6Ti1",
    "Cl5O6Ti2",
    "Cl5O6Ti3",
    "Cl5O7Ti2",
    "Cl5O7Ti3",
    "Cl5O8Ti3",
    "Cl5O9Ti3",
    "Cl5O10Ti3",
    "Cl5O1Ti1",
    "Cl5O1Ti2",
    "Cl5Ti1",
    "Cl6O2Ti1",
    "Cl6O2Ti2",
    "Cl6O3Ti1",
    "Cl6O3Ti2",
    "Cl6O3Ti3",
    "Cl6O4Ti2",
    "Cl6O4Ti1",
    "Cl6O4Ti3",
    "Cl6O5Ti1",
    "Cl6O5Ti2",
    "Cl6O5Ti3",
    "Cl6O6Ti1",
    "Cl6O6Ti2",
    "Cl6O6Ti3",
    "Cl6O7Ti2",
    "Cl6O7Ti3",
    "Cl6O8Ti3",
    "Cl6O9Ti3",
    "Cl6O10Ti3",
    "Cl6Ti1",
    "Cl6O1Ti1",
    "Cl7O2Ti3",
    "Cl6O1Ti2",
    "Cl7O3Ti3",
    "Cl7O4Ti3",
    "Cl7O5Ti3",
    "Cl7O6Ti3",
    "Cl7O7Ti3",
    "Cl7O8Ti3",
    "Cl7O9Ti3",
    "Cl7O10Ti3",
    "Cl8O2Ti3",
    "Cl8O3Ti3",
    "Cl8O4Ti3",
    "Cl8O5Ti3",
    "Cl8O6Ti3",
    "Cl8O7Ti3",
    "Cl8O8Ti3",
    "Cl8O9Ti3",
    "Cl8O10Ti3",
    "Cl1O2Ti2",
    "Cl1O2Ti1",
    "Cl1O3Ti1",
    "Cl1O3Ti2",
    "Cl1O4Ti1",
    "Cl1O4Ti2",
    "Cl1O4Ti3",
    "Cl1O5Ti1",
    "Cl1O5Ti2",
    "Cl1O5Ti3",
    "Cl1O6Ti1",
    "Cl1O6Ti2",
    "Cl1O6Ti3",
    "Cl1O7Ti2",
    "Cl1O7Ti3",
    "Cl1O8Ti3",
    "Cl1O9Ti3",
    "Cl1O10Ti3",
    "Cl1O1Ti2",
    "O2Ti2",
    "O3Ti1",
    "O3Ti2",
    "O4Ti1",
    "O4Ti2",
    "O4Ti3",
    "O5Ti1",
    "O5Ti2",
    "O5Ti3",
    "O6Ti1",
    "O6Ti2",
    "O6Ti3",
    "O7Ti2",
    "O7Ti3",
    "O8Ti3",
    "O9Ti3",
    "H1O5Ti1",
    "H1O4Ti1",
    "H2O5Ti1",
    "H2O4Ti1",
    "H2O3Ti1",
    "H3O5Ti1",
    "H3O3Ti1",
    "H4O2Ti1",
    "H3O2Ti1",
    "H4O5Ti1",
    "H5O5Ti1",
    "H4O4Ti1",
    "H4O1Ti1",
    "H5Ti1",
    "H1O6Ti1",
    "H3O6Ti1",
    "H4O6Ti1",
    "H5O6Ti1",
    "H5O1Ti1",
    "H6O6Ti1",
    "H6Ti1",
    "H5O3Ti1",
    "H2O6Ti1",
    "H6O5Ti1",
    "O1Ti2"
]


function get_random_question(){

const index = Math.floor(Math.random() * vibration_frequency.length);

    qst = '  show me the vibration frequency of ' + vibration_frequency[index]
    document.getElementById('input-field').value = qst;

}

let local_address = 'http://127.0.0.1:5000/'
let cmcl_address = 'https://kg.cmclinnovations.com/'
let address = cmcl_address

	$(window).on('load', function(){

	    let hostname = location.hostname;
	    console.log('host name is', hostname)

	    if (hostname.includes('127.0.0.1')){
	        address = local_address
	    }else{
	        address = cmcl_address
	    }
	    console.log('address is set to be', address)
	    google.charts.load('current', {'packages':['table']});
	    $('#google_result_box').hide()
	    $('#wolfram_result_box').hide()
	    $('#progress-container').hide()
	});

    (function($) {
    "use strict"; // Start of use strict

    // Smooth scrolling using jQuery easing
    $('a.js-scroll-trigger[href*="#"]:not([href="#"])').click(function() {
      if (location.pathname.replace(/^\//, '') == this.pathname.replace(/^\//, '') && location.hostname == this.hostname) {
        var target = $(this.hash);
        target = target.length ? target : $('[name=' + this.hash.slice(1) + ']');
        if (target.length) {
          $('html, body').animate({
            scrollTop: (target.offset().top - 71)
          }, 1000, "easeInOutExpo");
          return false;
        }
      }
    });

    // Scroll to top button appear
    $(document).scroll(function() {
      var scrollDistance = $(this).scrollTop();
      if (scrollDistance > 100) {
        $('.scroll-to-top').fadeIn();
      } else {
        $('.scroll-to-top').fadeOut();
      }
    });

    // Closes responsive menu when a scroll trigger link is clicked

    $('.sample-question').click(function(){
           // ask_question()
           let q =  $(this).text().replace('   ',' ');
           document.getElementById('input-field').value = q;
           $('html,body').scrollTop(0);

           // ask_question()
    });



    $('.js-scroll-trigger').click(function() {
      $('.navbar-collapse').collapse('hide');
    });

    // Activate scrollspy to add active class to navbar items on scroll
    $('body').scrollspy({
      target: '#mainNav',
      offset: 80
    });

    // Collapse Navbar
    var navbarCollapse = function() {
      if ($("#mainNav").offset().top > 100) {
        $("#mainNav").addClass("navbar-shrink");
      } else {
        $("#mainNav").removeClass("navbar-shrink");
      }
    };
    // Collapse now if page is not at top
    navbarCollapse();
    // Collapse the navbar when page is scrolled
    $(window).scroll(navbarCollapse);

  })(jQuery);

    var socket = io();

    socket.on('connect', function() {
        socket.emit('message', {data: 'I\'m connected!'});
    });

    socket.on('coordinate_agent', function(msg) {
        console.log('from coordinate agent', msg)
        update_log(msg)
    });


  var progress_counter = 1

  function update_log(msg){
     // TODO: update the log info
     if (msg.includes('Querying') && msg.includes('The World Avatar Knowledge Graph')){
        $('#query_progress').append('<div>' + msg + '</div>')
        console.log('updating the progress bar item ')
        $('#query_progress_bar').empty()
        $('#query_progress_bar').show()
        $('#query_progress').append('<div id="query_progress_bar">' + '#' + '</div>')

        setInterval(function(){

            progress_counter = progress_counter + 1;
            bar = '#'
            for (var i = 1; i < progress_counter; i++)
            {

                if (progress_counter < 20){
                    bar = bar + '#'
                }else{
                    progress_counter = 1
                }
            }
            console.log('progress bar', progress_counter)
            console.log('bar', bar)
            $('#query_progress_bar').html(bar)

         },2000);


     }
     else{
        $('#query_progress').append('<div>' + msg + '</div>')
        console.log('updating the progress', msg)
     }
  }




  function ask_question() {

    $('#query_progress').empty()
    $('#progress-container').show()

    document.getElementById('search-icon').style.display = 'none';
    document.getElementById('search-spinner').style.display = 'block';

    // =========================
    msg = $('#input-field').val();
    //$('#input-field').val(null);
    // =========================
    msg = msg.replace(/[/+]/g, 'add_sign')

   // to test the code locally, the address need to be changed to  http://127.0.0.1:5000/


    $('#search-results').hide()
    $('#google_result_box').hide()
    $('#wolfram_result_box').hide()


    query_wolfram_alpha(address, msg);
    query_google(address, msg);

    $.get(address + "query?question=" + msg, function( data ) {
      displayResults(data, 'jps')
    });

}



function process_json_result(result){

  // result = result.replace(/=\]/g, '=>').replace(/[}][\n ]+[{]/g, '},{')
  console.log('The request has returned a response ', result)
  result = JSON.parse(result)
  console.log('the result parsed', result, typeof(result) )


  if (result === 'Nothing'){
    console.log('Received nothing')
    update_log('The World Avatar failed to provide an answer')
    $('#query_progress_bar').html('')


    query_wolfram_alpha(address, msg);
    query_google(address, msg);
    return null
  }




  console.log('If it is nothing, you should not see this line')


  if (result){
      console.log('the result parsed', result, typeof(result) )
    if (typeof(result)!== 'object' && (result!== 'Nothing')){
      obj = '{"results": ' + result + '}'
      console.log(obj)
      r = JSON.parse(obj)
      console.log('the array',  r["results"])
        keys = []
        table = []
         console.log('this is a result from JPS', r)
         // get the variable names
         first_row = r["results"][0]
         head_object = {'result_id': 'index'}
         for (let head in first_row){
            head_object[head] = head
         }
         table.push(head_object)

        r["results"].forEach(function (item, index) {
         let row_object = {}


           for (let key in item) {
                 console.log(key, item[key]);
           counter = index + 1
           console.log(item, index);
           row_object['result_id'] = counter.toString()
           row_object[key] = item[key]
           }
            table.push(row_object)

        });
          console.log('------------- jps table ---------------')
          console.log(table)
    return table
  }


    if ('results' in result && !('Nothing' in result)){
    bindings = result.results.bindings;
    if (bindings.length == 0){
        // make a request to google or wolfram alpha
        return null
    }else

	{
        variables = result.head.vars
        table = []
		index_counter = 0
        bindings.forEach(function(v){
            let row = []
			let row_object = {}
			index_counter++
            row_object['result_id'] = index_counter.toString()

            if (v['oLabel']){
            			row_object['result_name'] = v['oLabel']['value']
            }

             if (v['name']){
            			row_object['result_name'] = v['name']['value']
            }


               if (v['v']){
			row_object['result_value'] = v['v']['value']
            }


			if (v['v2'])
		    {
		        row_object['result_value_2'] = v['v2']['value']
		    }

            if (v['unitLabel'])

		    {
			    row_object['result_unit'] = v['unitLabel']['value']
		    }


            table.push(row_object)
        })

		console.log('table', table)
        return table
    }
  }else{
  // get a list of variables, which is the keys
  variables = Object.keys(result[0]);
  console.log('variables', variables)
  index_counter = 0
  result.forEach(function(v){
      row = Object.values(v)
	  let row_obj = {}
	  row_obj['result_id'] = index_counter.toString()
	  row_obj['result_name'] = v
	  row_obj['result_value'] = row
      table.push(row_obj)
  })
  console.log('------------- table ---------------')
  console.log(table)
  return table
  }
  }

  else{
    // call wolfram_alpha or google
    console.log('No valid result returned')
  }
}
// if the query to the JPS fails, the system queries both wolfram_alpha and google at the same time
function query_wolfram_alpha(address, msg){
    $.get(address + "query_wolfram?question=" + msg, function( data ) {
      visualize_wolfram_result(data, 'wolfram')
    });
}

function query_google(address, msg){
    // the result returned by google will be in the form of html divisions, the visualization will be different
        $.get(address + "query_google?question=" + msg, function( data ) {
         visualize_google_result(data, 'google')
    });
}



function visualize_google_result(result){
    $('#google_result_box').show()
    if (result.trim() === ''){
        $("#google-results" ).html('<div class="div-row">Google failed to provide a direct answer</div>')
        $('#query_progress').append('<div>Google failed to provide a direct answer</div>')
    }else{
        //div = '<div class="div-row">' + result + '</div>'
        div = result
        $("#google-results" ).html(div)
    }
}

function visualize_wolfram_result(result){
    $('#wolfram_result_box').show()
        if (result.trim() === ''){
        $("#wolfram-results" ).html('<div class="div-row">Google failed to provide a direct answer</div>')
        $('#query_progress').append('<div>Wolfram alpha failed to provide a direct answer</div>')
    }else{
        div = '<div class="div-row">' + result + '</div>'
        $("#wolfram-results" ).html(div)
    }
   }


// TODO: query wolfram alpha and google no matter what
// TODO: Make the page Marie Curie
function removeItemAll(arr, value) {
  var i = 0;
  while (i < arr.length) {
      if(arr[i] === value) {
          arr.splice(i, 1);
      } else {
          ++i;
      }
  }
  return arr;
}

function drawTable(result_array) {
  console.log('result array', result_array)
  variables = result_array[0]
  rows = result_array[1]
  console.log('rows received', rows);
  first_col = rows[0];
  col_size = first_col.length;

  console.log('col size', col_size)
  var data = new google.visualization.DataTable();

/*
  if ((rows.length == 1) && (col_size == 1))
  {
      $('#single_div').val(rows[0][0])

  } */
  //else{
  for (col = 0; col < col_size; col++)
  {
    data.addColumn('string', variables[col]);
    console.log('added col' + variables[col])
  }

  data.addRows(rows);
  table_element = document.getElementById('table_div')
  // table_element.style.color = 'black';
  var table = new google.visualization.Table(table_element);

  var options =
  {
    showRowNumber: false,
    width: '100%',
    height: '100%',
    alternatingRowStyle: false,
    allowHtml: true,
    cssClassNames: {
      tableCell: 'cell',
      headerCell: 'headerCell'
    }
  };
  table.draw(data, options);



//  }

}

function displayResults(myData, source) {
     myData = process_json_result(myData)

  $('#query_progress_bar').html('')
  $('#query_progress_bar').hide()
  update_log('Obtained result from the World Avatar KG')

  // EXTRACT VALUE FOR HTML HEADER.
  // ('Book ID', 'Book Name', 'Category' and 'Price')
  var col = [];
  for (var i = 0; i < myData.length; i++) {
      for (var key in myData[i]) {
          if (col.indexOf(key) === -1) {
              col.push(key);
          }
      }
  }

  var divContainer = document.getElementById("search-results");
  divContainer.innerHTML = "";

  var h = document.createElement("H1")                // Create a <h1> element
  h.setAttribute("id", "result");
  if (source == 'wolfram'){
    h.innerHTML = 'Results (from wolfram alpha)'
  }
  else{
    var t = document.createTextNode("Results (from The World Avatar)");     // Create a text node
  h.appendChild(t);
  }

  divContainer.appendChild(h);

  // ADD JSON DATA TO THE TABLE AS ROWS.
  for (var i = 0; i < myData.length; i++) {

      var div_row = document.createElement("div");
      div_row.classList.add('div-row');

      for (var j = 0; j < col.length; j++) {
        // Create the list item:
        var div_inner = document.createElement('div');


        var data = myData[i][col[j]];





        if (data.includes('.svg') || data.includes('.png')){
//            var myImage = $('<img/>');
//            myImage.attr('src', data);

            div_inner.innerHTML = '<img src="' + data + '" style="width:250px">'


        }
        else{

                    if (data.includes('.g09') || data.includes('.xml')){
                    div_inner.innerHTML = '<a href="'+ data +'">'+ data +'</a>'
                // <a href="url">link text</a>
        }else{


                div_inner.appendChild(document.createTextNode(data));
}
        }

        // Set its contents:

        // Add it to the list:

        div_row.appendChild(div_inner);
      }
      divContainer.appendChild(div_row);
  }

  // FINALLY ADD THE NEWLY CREATED TABLE WITH JSON DATA TO A CONTAINER.

  document.getElementById('search-spinner').style.display = 'none';
  document.getElementById("search-results").style.display = "block";
  document.getElementById('search-icon').style.display = '';

// TODO: install javascript plugin in Pycharm ...
// No, this feature is for Pro ...

};